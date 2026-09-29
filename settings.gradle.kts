pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

rootProject.name = "Eurybium"
include("core", "compat-api", "processors", "minecraft")

val targets = providers.gradleProperty("minecraft_targets").get().split(',').map(String::trim)
stonecutter {
    create(":minecraft") {
        versions(targets)
        vcsVersion = targets.first()
    }
}
