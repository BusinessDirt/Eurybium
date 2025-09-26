import gg.essential.gradle.util.*



plugins {
    val kotlinVersion = "2.0.21"

    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.power-assert") version kotlinVersion apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("gg.essential.loom") version "1.9.+" apply false
    id("gg.essential.multi-version.root")
    id("gg.essential.multi-version.api-validation")
}

allprojects {
    group = "github.businessdirt"
    version = "0.1.0"

    repositories {
        mavenCentral()
        mavenLocal()

        // Fabric
        exclusiveContent {
            forRepository {
                maven("https://maven.fabricmc.net")
            }
            filter {
                includeGroup("net.fabricmc")
                includeGroup("net.fabricmc.fabric-api")
            }
        }

        // Mixin
        exclusiveContent {
            forRepository {
                maven("https://repo.spongepowered.org/repository/maven-public")
            }
            filter {
                includeGroup("org.spongepowered")
            }
        }

        // DevAuth
        exclusiveContent {
            forRepository {
                maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
            }
            filter {
                includeGroup("me.djtheredstoner")
            }
        }

        // Moulconfig
        exclusiveContent {
            forRepository {
                maven("https://maven.notenoughupdates.org/releases")
            }
            filter {
                includeGroup("org.notenoughupdates")
                includeGroup("org.notenoughupdates.moulconfig")
            }
        }
    }
}

preprocess {
    strictExtraMappings.set(true)

    val fabric12105 = createNode("1.21.5-fabric", 12105, "yarn")
}