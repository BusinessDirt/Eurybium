import java.util.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.loom)
}

val targetProperties: Properties = providers.fileContents(layout.projectDirectory.file("gradle.properties")).asText.map { text ->
    Properties().apply { load(text.reader()) }
}.get()

fun targetProperty(name: String): String = requireNotNull(targetProperties.getProperty(name)) {
    "Missing $name in ${project.projectDir}/gradle.properties"
}

val minecraftVersion = targetProperty("minecraft_version")
// MoulConfig artifacts follow the Minecraft release series (26.1.2 -> modern-26.1).
val moulconfigTarget = minecraftVersion.split('.').take(2).joinToString(".")
val minecraftRange = targetProperty("minecraft_range")
val fabricApiVersion = targetProperty("fabric_api_version")
val universalcraftTarget = targetProperty("universalcraft_target")
val universalcraftVersion = targetProperty("universalcraft_version")

base.archivesName = rootProject.name.lowercase()
version = "${rootProject.version}+mc$minecraftVersion"

repositories {
    maven("https://maven.notenoughupdates.org/releases/") {
        content { includeGroup("org.notenoughupdates.moulconfig") }
    }

    maven("https://repo.hypixel.net/repository/Hypixel/") {
        content { includeGroup("net.hypixel") }
    }

    maven("https://repo.essential.gg/repository/maven-public") {
        content { includeGroup("gg.essential") }
    }

    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        content { includeGroup("me.djtheredstoner") }
    }
}

dependencies {
    // Minecraft & Fabric
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation(libs.fabric.loader)
    implementation(libs.fabric.kotlin)
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // MoulConfig
    val moulconfig = "org.notenoughupdates.moulconfig:modern-$moulconfigTarget:${libs.versions.moulconfig.get()}"
    implementation(moulconfig)
    include(moulconfig)

    // HypixelModApi
    implementation(libs.hypixel.mod.api)

    // UniversalCraft
    val universalcraft = "gg.essential:universalcraft-$universalcraftTarget:$universalcraftVersion"
    implementation(universalcraft)
    include(universalcraft)

    // DevAuth
    runtimeOnly(libs.devauth)

    // KSP
    ksp(project(":processors"))
    compileOnly(project(":processors"))
    ksp(libs.auto.service.ksp)
    implementation(libs.auto.service.annotations)

    // Tests
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
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

ksp {
    arg("autoserviceKsp.verify", "true")
    arg("eurybium.version", rootProject.version.toString())
    arg("eurybium.id", rootProject.name.lowercase())
    arg("eurybium.name", rootProject.name)
}

// Tests consume the metadata generated for main; do not generate a second copy.
tasks.matching { it.name == "kspTestKotlin" }.configureEach { enabled = false }

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.FAIL
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
}

// Optional developer tasks; world downloads never run as part of a normal build.
extra["devWorldSavesDirectory"] = loom.runs.named("client").flatMap { it.runDirectory }.map { it.dir("saves") }
apply(from = rootProject.file("gradle/dev-worlds.gradle.kts"))
