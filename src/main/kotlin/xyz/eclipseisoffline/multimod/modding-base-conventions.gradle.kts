package xyz.eclipseisoffline.multimod

plugins {
    `java-library`
}

val modConfiguration = project.extensions.create<ModConfigurationExtension>("mod")

modConfiguration.targetJavaVersion.convention(21)

repositories {
    mavenCentral()
    maven {
        name = "Mojang"
        url = uri("https://libraries.minecraft.net")
    }
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
}

modConfiguration.afterConfiguration {
    base {
        archivesName = modConfiguration.archivesBaseName.get()
    }

    java {
        withSourcesJar()

        sourceCompatibility = JavaVersion.toVersion(modConfiguration.targetJavaVersion.get())
        targetCompatibility = JavaVersion.toVersion(modConfiguration.targetJavaVersion.get())
    }

    tasks {
        processResources {
            inputs.property("mod_id", modConfiguration.id.get())
            inputs.property("mod_name", modConfiguration.name.get())
            inputs.property("mod_description", modConfiguration.description.get())
            inputs.property("version", project.version)
            inputs.property("minecraft_version", modConfiguration.supportedMinecraftVersions.get())
            inputs.property("loader_version", modConfiguration.fabricLoader.get().get().version)
            inputs.property("fabric_api_version", modConfiguration.fabricApi.get().get().version)
            inputs.property("modrinth_id", modConfiguration.modrinthId.get())
            inputs.property("github_repository", modConfiguration.githubRepository.get())
            filteringCharset = "UTF-8"

            filesMatching("fabric.mod.json") {
                expand(
                    "mod_id" to modConfiguration.id.get(),
                    "mod_name" to modConfiguration.name.get(),
                    "mod_description" to modConfiguration.description.get(),
                    "version" to project.version,
                    "minecraft_version" to modConfiguration.supportedMinecraftVersions.get(),
                    "loader_version" to modConfiguration.fabricLoader.get().get().version,
                    "fabric_api_version" to modConfiguration.fabricApi.get().get().version,
                    "modrinth_id" to modConfiguration.modrinthId.get(),
                    "github_repository" to modConfiguration.githubRepository.get()
                )
            }
        }

        withType<JavaCompile>().configureEach {
            options.release = modConfiguration.targetJavaVersion.get()
        }

        jar {
            inputs.property("archivesName", project.base.archivesName)

            from("LICENSE") {
                rename {
                    "${it}_${project.base.archivesName}"
                }
            }
        }
    }
}
