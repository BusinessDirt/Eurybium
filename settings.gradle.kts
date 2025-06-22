pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://maven.architectury.dev")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net")
    }

    plugins {
        val egtVersion = "0.6.10"
        id("gg.essential.multi-version.root") version egtVersion
        id("gg.essential.multi-version.api-validation") version egtVersion
    }

    resolutionStrategy.eachPlugin {
        when (requested.id.id) {
            "gg.essential.loom" -> useModule("gg.essential:architectury-loom:${requested.version}")
        }
    }
}

rootProject.name = "Eurybium"
rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.21.5-fabric",
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}

