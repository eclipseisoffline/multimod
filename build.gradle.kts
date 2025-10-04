plugins {
    `java-gradle-plugin`
    `maven-publish`
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
    implementation(libs.moddev.gradle)
    implementation(libs.fabric.loom)
    implementation(libs.mod.publish.plugin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

gradlePlugin {
    plugins {
        create("multimod") {
            id = "xyz.eclipseisoffline.multimod"
            implementationClass = "xyz.eclipseisoffline.multimod.MultiModGradlePlugin"
        }
    }
}

publishing {

}
