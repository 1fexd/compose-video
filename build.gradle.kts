import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    id("com.android.application") version "8.4.0" apply false
    id("com.android.library") version "8.4.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false

    id("com.diffplug.spotless") version "6.25.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jetbrains.dokka") version "1.7.20"
}

tasks.withType(DokkaMultiModuleTask::class).configureEach {
    outputDirectory.set(rootProject.file("docs/api"))
    failOnWarning.set(true)
}

apply(from = "${rootDir}/scripts/generate-dokka.gradle")

subprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")

            ktlint("0.48.2")
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }
    }
}
