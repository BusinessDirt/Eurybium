import groovy.json.JsonSlurper
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.net.URI
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.security.MessageDigest
import java.util.zip.ZipFile

/** Explicit developer actions; downloaded data and editable saves must never become cached task outputs. */
@DisableCachingByDefault(because = "Installs editable development saves from an external index")
abstract class DevWorldTask : DefaultTask() {

    @get:Internal abstract val indexSource: Property<String>
    @get:Internal abstract val worldId: Property<String>
    @get:Internal abstract val savesDirectory: DirectoryProperty
    @get:Internal abstract val cacheDirectory: DirectoryProperty
    @get:Internal abstract val offline: Property<Boolean>
    @get:Internal abstract val install: Property<Boolean>

    private data class World(
        val id: String,
        val name: String,
        val url: URI,
        val sha256: String,
    )

    @TaskAction
    fun run() {
        val selectedId = worldId.orNull
        if (install.get() && selectedId.isNullOrBlank())
            throw GradleException("Select a world with -Pworld=<id>. Run listDevWorlds to see available IDs.")

        val worlds = readWorlds()
        if (!install.get()) {
            if (worlds.isEmpty()) logger.lifecycle("No development worlds have been published in this index.")
            worlds.forEach { logger.lifecycle("${it.id} | ${it.name}") }
            return
        }

        val world = worlds.singleOrNull { it.id == selectedId }
            ?: throw GradleException("Unknown world '$selectedId'. Run listDevWorlds to see available IDs.")

        val saves = savesDirectory.get().asFile.toPath()
        val destination = saves.resolve(world.id)
        check(!Files.exists(destination, NOFOLLOW_LINKS)) {
            "Save already exists: $destination. Move or remove it yourself before installing a fresh copy."
        }

        val archive = verifiedArchive(world)
        Files.createDirectories(saves)
        // Extract beside the destination so a failed download or ZIP never leaves a partial installed save.
        val staging = Files.createTempDirectory(saves, ".dev-world-")
        try {
            ZipFile(archive.toFile()).use { zip ->
                for (entry in zip.entries().asSequence()) {
                    val output = staging.resolve(entry.name).normalize()
                    check(!entry.name.contains('\\') && !entry.name.contains(':') && output.startsWith(staging)) {
                        "Unsafe path in world ZIP: ${entry.name}"
                    }

                    if (entry.isDirectory) {
                        Files.createDirectories(output)
                    } else {
                        Files.createDirectories(output.parent)
                        zip.getInputStream(entry).use { input ->
                            Files.newOutputStream(output, CREATE_NEW).use { input.copyTo(it) }
                        }
                    }
                }
            }

            // Some archives contain the save directly; others wrap it in a named world folder.
            // Move the actual save, so level.dat always ends up directly inside saves/<id>/.
            val extractedWorld = if (isWorldDirectory(staging)) {
                staging
            } else {
                val candidates = Files.list(staging).use { entries ->
                    entries.filter { Files.isDirectory(it) && isWorldDirectory(it) }.toList()
                }

                check(candidates.size == 1) {
                    "World ZIP must contain level.dat at its root or in exactly one top-level world folder."
                }

                candidates.single()
            }

            // No REPLACE_EXISTING: installing a fixture must not overwrite an edited development save.
            Files.move(extractedWorld, destination)
            logger.lifecycle("Installed '${world.id}' into $destination")
        } finally {
            staging.toFile().deleteRecursively()
        }
    }

    private fun isWorldDirectory(directory: Path): Boolean {
        val levelData = directory.resolve("level.dat")
        return Files.isRegularFile(levelData) && Files.size(levelData) > 0
    }

    private fun readWorlds(): List<World> {
        val source = URI(indexSource.get())
        val cached = cacheDirectory.get().asFile.toPath().resolve("indexes/${digest(indexSource.get().toByteArray())}.json")
        val indexFile = when {
            source.scheme == "file" -> Path.of(source)
            offline.get() -> {
                check(Files.isRegularFile(cached)) {
                    "No cached world index. Run online once or pass -PdevWorldIndex=/path/to/dev/worlds.json."
                }
                cached
            }
            else -> {
                download(source, cached)
                cached
            }
        }

        val index = JsonSlurper().parse(indexFile.toFile(), "UTF-8") as? Map<*, *>
            ?: error("World index must be a JSON object.")

        check(index["schemaVersion"] == 1) { "Unsupported world index schemaVersion; expected 1." }

        val entries = index["worlds"] as? List<*> ?: error("World index must contain a worlds array.")
        val worlds = entries.map { entry ->
            val data = entry as? Map<*, *> ?: error("World entry must be a JSON object.")
            val download = data["download"] as? Map<*, *> ?: error("World entry must contain download metadata.")
            val world = World(
                data.text("id"), data.text("name"),
                URI(download.text("url")), download.text("sha256"),
            )

            // The ID also becomes the save folder name; reject paths and traversal segments.
            check(world.id.matches(Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*"))) { "Invalid world ID: ${world.id}" }
            check(world.sha256.matches(Regex("[a-f0-9]{64}"))) { "Invalid SHA-256 checksum for '${world.id}'." }

            // Local archives are useful for testing an unpublished local index, never for a remote index.
            check(world.url.scheme == "https" || (source.scheme == "file" && world.url.scheme == "file")) {
                "World '${world.id}' must have an HTTPS download URL. Local indexes may also use file: URLs."
            }

            world
        }

        check(worlds.map { it.id }.distinct().size == worlds.size) { "Duplicate world IDs in index." }
        return worlds
    }

    private fun verifiedArchive(world: World): Path {
        val archive = cacheDirectory.get().asFile.toPath().resolve("archives/${world.sha256}.zip")
        if (Files.isRegularFile(archive) && checksum(archive) == world.sha256) return archive
        check(!offline.get() || world.url.scheme == "file") {
            "No valid cached ZIP for '${world.id}'. Run online once to download it."
        }

        download(world.url, archive, world.sha256)

        return archive
    }

    /** Downloads to a temporary file and verifies it before replacing any cached copy. */
    private fun download(source: URI, destination: Path, expectedChecksum: String? = null) {
        check(source.scheme in listOf("https", "file")) { "Only HTTPS or local file sources are supported." }

        Files.createDirectories(destination.parent)

        val temporary = Files.createTempFile(destination.parent, ".download-", ".tmp")
        try {
            if (source.scheme == "file") {
                Files.copy(Path.of(source), temporary, REPLACE_EXISTING)
            } else {
                val connection = source.toURL().openConnection().apply {
                    connectTimeout = 15_000
                    readTimeout = 60_000
                    setRequestProperty("User-Agent", "Eurybium-DevWorlds")
                }

                connection.getInputStream().use { input ->
                    Files.newOutputStream(temporary).use { input.copyTo(it) }
                }
            }

            check(expectedChecksum == null || checksum(temporary) == expectedChecksum) {
                "Downloaded world checksum does not match its index entry; no save was installed."
            }

            Files.move(temporary, destination, REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    private fun Map<*, *>.text(key: String): String =
        (this[key] as? String)?.takeIf { it.isNotBlank() } ?: error("Missing or empty '$key' in world metadata.")

    private fun checksum(file: Path): String {
        val hash = MessageDigest.getInstance("SHA-256")

        Files.newInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                hash.update(buffer, 0, count)
            }
        }

        return hash.digest().joinToString("") { "%02x".format(it) }
    }

    private fun digest(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}

val dataRef = providers.gradleProperty("devWorldRef").orElse("master")
val configuredIndexSource = providers.gradleProperty("devWorldIndex").map { source ->
    if (source.startsWith("https://") || source.startsWith("file:")) source
    else rootProject.file(source).toURI().toString()
}.orElse(dataRef.map { "https://raw.githubusercontent.com/BusinessDirt/Eurybium-Data/$it/dev/worlds.json" })

@Suppress("UNCHECKED_CAST")
val targetSavesDirectory = extra["devWorldSavesDirectory"] as Provider<Directory>

tasks.register<DevWorldTask>("listDevWorlds") {
    group = "development worlds"
    description = "Lists available development worlds from the Eurybium data index."
    install.set(false)
}

tasks.register<DevWorldTask>("installDevWorld") {
    group = "development worlds"
    description = "Installs the world selected with -Pworld=<id> into the client saves directory."
    install.set(true)
}

tasks.withType<DevWorldTask>().configureEach {
    indexSource.set(configuredIndexSource)
    worldId.set(providers.gradleProperty("world"))
    savesDirectory.set(targetSavesDirectory)
    cacheDirectory.set(rootProject.layout.projectDirectory.dir(".gradle/dev-worlds"))
    offline.set(gradle.startParameter.isOffline)
}
