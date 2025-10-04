package xyz.eclipseisoffline.multimod

import net.fabricmc.loom.task.RemapJarTask
import org.gradle.kotlin.dsl.getByName

plugins {
    id("xyz.eclipseisoffline.multimod.modding-base-conventions")
    id("fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val modConfiguration = project.extensions.getByType(ModConfigurationExtension::class)

modConfiguration.afterConfiguration {
    dependencies {
        minecraft(modConfiguration.minecraft.get())
        mappings(loom.layered {
            officialMojangMappings()
            if (modConfiguration.parchment.isPresent) {
                parchment(modConfiguration.parchment.get())
            }
        })

        modImplementation(modConfiguration.fabricLoader.get())

        if (modConfiguration.fabricApi.isPresent) {
            modImplementation(modConfiguration.fabricApi.get())
        }
    }

    publishing {
        repositories {
            addAll(modConfiguration.mavenRepositories.get())
        }

        publications {
            create<MavenPublication>("maven") {
                artifactId = project.base.archivesName.get()
                from(components["java"])
            }
        }
    }

    publishMods {
        changelog = File("CHANGELOG.md").readText()
        type = modConfiguration.releaseType.get()

        file = tasks.getByName<RemapJarTask>("remapJar").archiveFile
        modLoaders.add("fabric")

        modrinth {
            accessToken = providers.gradleProperty("MODRINTH_API_TOKEN")
            projectId = modConfiguration.modrinthId.get()
            minecraftVersions.addAll(modConfiguration.releaseVersions.get().split(","))
        }

        github {
            accessToken = providers.gradleProperty("GITHUB_API_PUBLISH_TOKEN")
            repository = modConfiguration.githubRepository.get()
            commitish = modConfiguration.gitBranch.get()
        }
    }
}
