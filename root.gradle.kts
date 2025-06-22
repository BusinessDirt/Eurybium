import gg.essential.gradle.util.*

plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.power-assert") version "2.0.21" apply false
    id("gg.essential.loom") version "1.9.+" apply false
    id("gg.essential.multi-version.root")
    id("gg.essential.multi-version.api-validation")
}

version = "0.1.0"

preprocess {
    strictExtraMappings.set(true)

    val fabric12105 = createNode("1.21.5-fabric", 12105, "yarn")
}