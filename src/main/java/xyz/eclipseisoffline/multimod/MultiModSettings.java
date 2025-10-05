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

    public MultiModSettings(ObjectFactory factory) {
        defaultRepositories = repositories -> {
            repositories.mavenCentral();
            repositories.maven(maven -> {
                maven.setName("Mojang");
                maven.setUrl("https://libraries.minecraft.net");
            });
            repositories.maven(maven -> {
                maven.setName("ParchmentMC");
                maven.setUrl("https://maven.parchmentmc.org");
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
    }

    public void repositories(Action<? super RepositoryHandler> repositories) {
        defaultRepositories = repositories;
    }
}
