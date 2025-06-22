import gg.essential.gradle.util.*

plugins {
    idea
    java
    kotlin("jvm")
    kotlin("plugin.power-assert")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("gg.essential.defaults.maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    // JUnit Jupiter API + Engine
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    val vigilanceVersion = 312
    implementation("gg.essential:vigilance:${vigilanceVersion}")

    val universalCraftVersion = 427
    modImplementation("gg.essential:universalcraft-$platform:$universalCraftVersion")

    val elementaVersion = 710
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

    test {
        useJUnitPlatform()
    }

    shadowJar {
        archiveClassifier.set(null as String?)
        relocate("gg.essential.elementa.unstable", "github.businessdirt.elementa.unstable")
    }

    build {
        dependsOn(shadowJar)
    }
}