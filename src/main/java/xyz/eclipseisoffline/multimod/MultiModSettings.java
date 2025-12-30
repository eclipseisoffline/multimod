package xyz.eclipseisoffline.multimod;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class MultiModSettings {
    public final Property<Boolean> includeCommonRepositories;
    public final Property<Boolean> buildSourcesJar;
    public final Property<Boolean> setJavaVersion;
    public final Property<Boolean> configureResources;
    public final Property<Boolean> includeLicenseInJar;
    public final Property<Boolean> disableNeoForgeRecompilation;

    public MultiModSettings(ObjectFactory factory) {
        includeCommonRepositories = factory.property(Boolean.class).convention(true);
        buildSourcesJar = factory.property(Boolean.class).convention(true);
        setJavaVersion = factory.property(Boolean.class).convention(true);
        configureResources = factory.property(Boolean.class).convention(true);
        includeLicenseInJar = factory.property(Boolean.class).convention(true);
        disableNeoForgeRecompilation = factory.property(Boolean.class).convention(true);
    }

    public void from(MultiModSettings other) {
        includeCommonRepositories.convention(other.includeCommonRepositories);
        buildSourcesJar.convention(other.buildSourcesJar);
        setJavaVersion.convention(other.setJavaVersion);
        configureResources.convention(other.configureResources);
        includeLicenseInJar.convention(other.includeLicenseInJar);
        disableNeoForgeRecompilation.convention(other.disableNeoForgeRecompilation);
    }
}
