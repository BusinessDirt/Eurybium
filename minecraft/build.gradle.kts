import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom)
}

val targetProperties = providers.fileContents(layout.projectDirectory.file("gradle.properties")).asText.map { text ->
    Properties().apply { load(text.reader()) }
}.get()
fun targetProperty(name: String): String = requireNotNull(targetProperties.getProperty(name)) {
    "Missing $name in ${project.projectDir}/gradle.properties"
}

val minecraftVersion = targetProperty("minecraft_version")
val minecraftRange = targetProperty("minecraft_range")
val fabricApiVersion = targetProperty("fabric_api_version")
val universalcraftTarget = targetProperty("universalcraft_target")
val universalcraftVersion = targetProperty("universalcraft_version")

base.archivesName = "eurybium"
version = "${rootProject.version}+mc$minecraftVersion"

repositories {
    maven("https://repo.essential.gg/repository/maven-public") {
        content { includeGroup("gg.essential") }
    }
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        content { includeGroup("me.djtheredstoner") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation(libs.fabric.loader)
    implementation(libs.fabric.kotlin)
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation(project(":core"))
    implementation(project(":compat-api"))

    val universalcraft = "gg.essential:universalcraft-$universalcraftTarget:$universalcraftVersion"
    implementation(universalcraft)
    include(universalcraft)

    // Development only: never included in the distributed mod.
    runtimeOnly(libs.devauth)
}

loom {
    runs {
        named("client") {
            client()
            generateRunConfig = true
            runDirectory = layout.projectDirectory.dir("run")
            systemProperties.put("devauth.configDir", rootProject.file(".devauth").absolutePath)
            systemProperties.put("devauth.enabled", providers.gradleProperty("devauth").orElse("false").get())
        }
        removeIf { it.name == "server" }
    }
}

val metadata = mapOf(
    "version" to rootProject.version.toString(),
    "minecraft" to minecraftRange,
    "loader" to libs.versions.fabric.loader.get(),
    "fabric_kotlin" to libs.versions.fabric.kotlin.get(),
    "fabric_api" to fabricApiVersion,
)
tasks.processResources {
    inputs.properties(metadata)
    filesMatching("fabric.mod.json") { expand(metadata) }
}

// Embed only our plain JVM modules. Kotlin is supplied by Fabric Language Kotlin.
val sharedProjects = listOf(project(":core"), project(":compat-api"))
tasks.jar {
    sharedProjects.forEach { shared ->
        val sharedJar = shared.tasks.named<Jar>("jar")
        dependsOn(sharedJar)
        from(sharedJar.map { zipTree(it.archiveFile) }) {
            exclude("META-INF/MANIFEST.MF")
        }
    }
    duplicatesStrategy = DuplicatesStrategy.FAIL
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
}
