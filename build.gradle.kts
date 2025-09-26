import gg.essential.gradle.util.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    kotlin("jvm")
    kotlin("plugin.power-assert")
    id("gg.essential.loom")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("gg.essential.defaults.maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.google.devtools.ksp")
}

val javaVersion = 21
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion)) // TODO: per version
}

loom {
    val accessWidenerFile = file("src/main/resources/eurybium.accesswidener")
    if (accessWidenerFile.exists()) {
        accessWidenerPath = accessWidenerFile
    }
}

sourceSets.main {
    resources.destinationDirectory.set(kotlin.destinationDirectory)
    output.setResourcesDir(kotlin.destinationDirectory)
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val shadowModImpl: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

java.withSourcesJar()
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
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")

    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
    implementation("com.google.auto.service:auto-service-annotations:1.1.1")

    val devAuthVersion = "1.2.1"
    modRuntimeOnly("me.djtheredstoner:DevAuth-$devAuthModule:$devAuthVersion")

    val moulconfigVersion = "4.1.1-beta"
    shadowModImpl("org.notenoughupdates.moulconfig:modern-${platform.mcVersionStr}:$moulconfigVersion")
    include("org.notenoughupdates.moulconfig:modern-${platform.mcVersionStr}:$moulconfigVersion")

    val universalCraftVersion = 427
    shadowModImpl("gg.essential:universalcraft-$platform:$universalCraftVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")

    shadowImpl("org.reflections:reflections:0.10.2")
    shadowImpl("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    shadowImpl("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
}

tasks {
    processResources {
        inputs.property("version", version)
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to version
            ))
        }
    }

    compileJava {
        dependsOn(processResources)
    }

    withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
    }

    withType(KotlinCompile::class.java) {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }

    test {
        useJUnitPlatform()
    }

    shadowJar {
        archiveClassifier.set("all-dev")
        configurations = listOf(shadowImpl, shadowModImpl)
        doLast {
            configurations.forEach {
                println("Config: ${ it.files }")
            }
        }

        exclude("META-INF/versions/**")
        mergeServiceFiles()
        relocate("io.github.notenoughupdates.moulconfig", "github.businessdirt.dependencies.moulconfig")
    }

    build {
        dependsOn(shadowJar)
    }
}