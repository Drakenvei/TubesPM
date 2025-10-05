pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.google.gms.google-services") version "4.4.3"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // <-- Ensure this line is here
        mavenCentral()
    }
}

rootProject.name = "TubesPM"
include(":app")
