plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "xyz.eclipseisoffline"
version = "0.2.0-SNAPSHOT"

val targetJavaVersion = 25

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
    val version = JavaVersion.toVersion(targetJavaVersion)

    sourceCompatibility = version
    targetCompatibility = version
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
    repositories {
        maven {
            name = "eclipseisoffline"
            url = when {
                (version.toString().endsWith("-SNAPSHOT") || System.getenv()["MULTIMOD_IS_DEV"]?.lowercase() == "true")
                    -> uri("https://maven.eclipseisoffline.xyz/snapshots")
                else -> uri("https://maven.eclipseisoffline.xyz/releases")
            }
            credentials(PasswordCredentials::class)
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = targetJavaVersion
    }

    jar {
        manifest {
            attributes["Implementation-Version"] = project.version
        }
    }
}
