pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "compose-video"
include(":compose-video")

if (System.getenv("JITPACK") != "true") {
    include(":sample")
    include(":compose-video-baselineprof")
}
