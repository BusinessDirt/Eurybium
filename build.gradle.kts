import gg.essential.gradle.util.*

plugins {
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("gg.essential.defaults.maven-publish")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

group = "github.businessdirt"

java.withSourcesJar()
tasks.compileKotlin.setJvmDefault("all")
loom.noServerRunConfigs()

val devAuthModule = when {
    platform.loaderStr == "fabric" -> "fabric"
    platform.loaderStr == "neoforge" -> "neoforge"
    platform.loaderStr == "forge" && platform.mcMajor == 1 && platform.mcMinor <= 12 -> "forge-legacy"
    platform.loaderStr == "forge" -> "forge-latest"
    else -> error("Unknown loader: ${platform.loaderStr}")
}

val fabricVersion: String by project

dependencies {
    val vigilanceVersion = 306
    implementation("gg.essential:vigilance:${vigilanceVersion}")

    val universalCraftVersion = 412
    modImplementation("gg.essential:universalcraft-$platform:$universalCraftVersion")

    val elementaVersion = 708
    implementation("gg.essential:elementa:$elementaVersion")
    implementation("gg.essential:elementa-unstable-statev2:$elementaVersion")

    val devAuthVersion = "1.2.1"
    modRuntimeOnly("me.djtheredstoner:DevAuth-$devAuthModule:$devAuthVersion")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
}

tasks {
    processResources {
        dependsOn(compileJava)
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to version
            ))
        }
    }
}