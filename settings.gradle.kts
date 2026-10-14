rootProject.name = "dormfix"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    // Lets Gradle download JDK 21 automatically if it isn't installed
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(":server") // Spring Boot backend (Java + Kotlin)
include(":web")    // Compose Multiplatform for Web frontend (Kotlin/Wasm)
