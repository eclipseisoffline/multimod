package xyz.eclipseisoffline.multimod;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class MinecraftSettings {
    public final Property<Dependency> minecraft;

    public final Property<Dependency> mixin;
    public final Property<Dependency> mixinExtras;

    public final Property<String> supportedMinecraftVersions;
    public final Property<String> neoForgeSupportedMinecraftVersions;

    public MinecraftSettings(Project project) {
        ObjectFactory factory = project.getObjects();

        minecraft = factory.property(Dependency.class);

        mixin = factory.property(Dependency.class);
        mixinExtras = factory.property(Dependency.class);

        supportedMinecraftVersions = factory.property(String.class);
        neoForgeSupportedMinecraftVersions = factory.property(String.class);

        mixin.convention(project.getDependencyFactory().create("org.spongepowered", "mixin", MultiModVersions.MIXIN_VERSION));
        mixinExtras.convention(project.getDependencyFactory().create("io.github.llamalad7", "mixinextras-common", MultiModVersions.MIXIN_EXTRAS_VERSION));

        supportedMinecraftVersions.convention(minecraft.map(Dependency::getVersion));
        neoForgeSupportedMinecraftVersions.convention(minecraft.map(Dependency::getVersion).map(version -> "[" + version + "]"));
    }
}
