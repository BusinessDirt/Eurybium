plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

dependencies {
    api(project(":compat-api"))
    ksp(project(":processors"))
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

ksp {
    arg("eurybium.version", project.version.toString())
}

// Tests consume the metadata generated for main; do not generate a second copy.
tasks.matching { it.name == "kspTestKotlin" }.configureEach { enabled = false }
