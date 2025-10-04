package xyz.eclipseisoffline.multimod

import me.modmuss50.mpp.ReleaseType
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface ModConfigurationExtension {
    val id: Property<String>
    val name: Property<String>
    val description: Property<String>

    val archivesBaseName: Property<String>

    val minecraft: Property<Provider<MinimalExternalModuleDependency>>
    val neoFormTimestamp: Property<String>
    val parchment: Property<Provider<MinimalExternalModuleDependency>>

    val fabricLoader: Property<Provider<MinimalExternalModuleDependency>>
    val fabricApi: Property<Provider<MinimalExternalModuleDependency>>

    val supportedMinecraftVersions: Property<String>

    val modrinthId: Property<String>
    val releaseType: Property<ReleaseType>
    val releaseVersions: Property<String>
    val githubRepository: Property<String>
    val gitBranch: Property<String>

    val mavenRepositories: ListProperty<MavenArtifactRepository>

    val targetJavaVersion: Property<Int>

    val afterConfiguration: ListProperty<Runnable>

    fun afterConfiguration(runnable: Runnable) {
        afterConfiguration.add(runnable)
    }

    fun finishConfiguring() {
        afterConfiguration.get().forEach {
            it.run()
        }
    }
}