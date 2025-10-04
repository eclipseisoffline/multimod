plugins {
    `kotlin-dsl`
}

group = "xyz.eclipseisoffline"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
}

dependencies {
    implementation(libs.neogradle)
    implementation(libs.fabric.loom)
    implementation(libs.mod.publish.plugin)
}
