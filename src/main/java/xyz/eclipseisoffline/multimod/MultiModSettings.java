package xyz.eclipseisoffline.multimod;

import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class MultiModSettings {
    Action<? super RepositoryHandler> defaultRepositories;

    public final Property<Boolean> buildSourcesJar;
    public final Property<Boolean> setJavaVersion;
    public final Property<Boolean> configureResources;
    public final Property<Boolean> includeLicenseInJar;
    public final Property<Boolean> disableNeoForgeRecompilation;

    public MultiModSettings(ObjectFactory factory) {
        defaultRepositories = repositories -> {
            repositories.mavenCentral();
            repositories.maven(maven -> {
                maven.setName("Mojang");
                maven.setUrl("https://libraries.minecraft.net");
            });
            repositories.maven(maven -> {
                maven.setName("Fabric");
                maven.setUrl("https://maven.fabricmc.net");
            });
            repositories.maven(maven -> {
                maven.setName("NeoForged");
                maven.setUrl("https://maven.neoforged.net/releases");
            });
        };

        buildSourcesJar = factory.property(Boolean.class).convention(true);
        setJavaVersion = factory.property(Boolean.class).convention(true);
        configureResources = factory.property(Boolean.class).convention(true);
        includeLicenseInJar = factory.property(Boolean.class).convention(true);
        disableNeoForgeRecompilation = factory.property(Boolean.class).convention(true);
    }

    // TODO fix this with from, can it be a property? is this method necessary?
    public void repositories(Action<? super RepositoryHandler> repositories) {
        defaultRepositories = repositories;
    }

    public void from(MultiModSettings other) {
        buildSourcesJar.convention(other.buildSourcesJar);
        setJavaVersion.convention(other.setJavaVersion);
        configureResources.convention(other.configureResources);
        includeLicenseInJar.convention(other.includeLicenseInJar);
        disableNeoForgeRecompilation.convention(other.disableNeoForgeRecompilation);
    }
}
