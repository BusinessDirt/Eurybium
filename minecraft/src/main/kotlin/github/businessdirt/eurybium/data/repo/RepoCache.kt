package github.businessdirt.eurybium.data.repo

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import github.businessdirt.eurybium.core.utils.files.RecoverableTextFile
import java.io.File

/** Raw source files are cached together so startup revalidates exactly the revision originally fetched. */
internal data class CachedRepo(
    val revision: String,
    val etag: String?,
    val fetchedAtMillis: Long,
    val files: Map<String, String>
) {
    fun snapshot(): RepoSnapshot = RepoParser.parse(revision, fetchedAtMillis, files)
}

/** One atomically replaced bundle; a partial update can never mix files from different commits. */
internal class RepoCache(private val file: File) {

    fun read(): CachedRepo? {
        if (!file.exists() && !File(file.path + ".bak").exists()) return null

        // Bound the encoded cache before allocating its contents, including the recovery copy.
        for (candidate in listOf(file, File(file.path + ".bak"))) {
            require(!candidate.exists() || candidate.length() <= MAX_CACHE_BYTES) { "Repository cache is too large" }
        }

        val root = JsonParser.parseString(RecoverableTextFile(file).load()).asJsonObject
        require(root.get("cacheVersion").asInt == 1) { "Unsupported repository cache" }

        val files = root.getAsJsonObject("files").entrySet().associate { it.key to it.value.asString }
        val etag = root.get("etag")?.takeUnless { it.isJsonNull }?.asString
        require(etag == null || (etag.length <= 1024 && '\r' !in etag && '\n' !in etag)) { "Invalid cache ETag" }

        return CachedRepo(root.get("revision").asString, etag, root.get("fetchedAtMillis").asLong, files)
    }

    fun save(repo: CachedRepo) {
        val root = JsonObject().apply {
            addProperty("cacheVersion", 1)
            addProperty("revision", repo.revision)
            addProperty("etag", repo.etag)
            addProperty("fetchedAtMillis", repo.fetchedAtMillis)
            add("files", JsonObject().apply { repo.files.forEach { (path, text) -> addProperty(path, text) } })
        }

        val text = Gson().toJson(root)
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_CACHE_BYTES) { "Repository cache is too large" }

        file.parentFile.mkdirs()

        RecoverableTextFile(file).save(text)
    }

    companion object {
        private const val MAX_CACHE_BYTES = 64L * 1024 * 1024
    }
}
