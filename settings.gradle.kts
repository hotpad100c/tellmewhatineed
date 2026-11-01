pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
    create(rootProject) {
        versions("1.21.7", "1.21.11").buildscript("build.gradle.kts")
        versions("26.1.2", "26.2", "26.3").buildscript("unobfuscated.gradle.kts")
        vcsVersion = "26.3"
    }
}

rootProject.name = "tellmewhatineed"
