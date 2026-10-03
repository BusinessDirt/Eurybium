import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    base
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.loom) apply false
}

allprojects {
    group = "github.businessdirt.eurybium"
    version = rootProject.providers.gradleProperty("mod_version").get()

    repositories {
        mavenCentral()
    }
}

subprojects {

    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(25)
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 25
    }
}

val targets = providers.gradleProperty("minecraft_targets").get()
    .split(',')
    .map(String::trim)

val modules = listOf(":processors") + targets.map { ":minecraft:$it" }

tasks.assemble { dependsOn(modules.map { "$it:assemble" }) }
tasks.check { dependsOn(modules.map { "$it:check" }) }
tasks.clean { dependsOn(modules.map { "$it:clean" }) }
