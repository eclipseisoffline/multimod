package xyz.eclipseisoffline.multimod;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MinecraftSettings {
    private static final Pattern MINECRAFT_RELEASE_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(\\.\\d+)?$");
    private static final Pattern MINECRAFT_SNAPSHOT_PATTERN = Pattern.compile("^(.+)-([A-z]+)-(\\d+)$");

    public final Property<Dependency> minecraft;

    public final Property<Dependency> mixin;
    public final Property<Dependency> mixinExtras;

    public final Property<String> supportedMinecraftVersions;
    public final Property<String> neoForgeSupportedMinecraftVersions;

    public final Property<Boolean> supportMinecraftHotfixes;

    public MinecraftSettings(Project project) {
        ObjectFactory factory = project.getObjects();

        minecraft = factory.property(Dependency.class);

        mixin = factory.property(Dependency.class);
        mixinExtras = factory.property(Dependency.class);

        supportedMinecraftVersions = factory.property(String.class);
        neoForgeSupportedMinecraftVersions = factory.property(String.class);

        supportMinecraftHotfixes = factory.property(Boolean.class);

        mixin.convention(project.getDependencyFactory().create("org.spongepowered", "mixin", MultiModVersions.MIXIN_VERSION));
        mixinExtras.convention(project.getDependencyFactory().create("io.github.llamalad7", "mixinextras-common", MultiModVersions.MIXIN_EXTRAS_VERSION));

        supportedMinecraftVersions.convention(minecraft
                .map(Dependency::getVersion)
                .map(version -> {
                    if (supportMinecraftHotfixes.getOrElse(false)) {
                        return getMinecraftRelease(version)
                                .map(release -> "~" + release)
                                .orElseGet(() -> normaliseMinecraftVersionForFabric(version));
                    }
                    return normaliseMinecraftVersionForFabric(version);
                }));
        neoForgeSupportedMinecraftVersions.convention(minecraft
                .map(Dependency::getVersion)
                .map(version -> {
                    if (supportMinecraftHotfixes.getOrElse(false)) {
                        return getMinecraftRelease(version)
                                .map(release -> "[" + release + ", " + bumpMinorMinecraftRelease(release) + ")")
                                .orElseGet(() -> "[" + version + "]");
                    }
                    return "[" + version + "]";
                }));

        supportMinecraftHotfixes.convention(true);
    }

    public void supported(Provider<String> versions) {
        supportedVersionList(versions.map(list -> List.of(list.split(","))));
    }

    public void supportedVersionList(Provider<List<String>> versions) {
        supportedMinecraftVersions.set(versions.map(list -> {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Must specify at least one supported Minecraft version");
            } else if (list.size() == 1) {
                return normaliseMinecraftVersionForFabric(list.getFirst());
            } else {
                return ">=" + normaliseMinecraftVersionForFabric(list.getFirst()) + " <=" + normaliseMinecraftVersionForFabric(list.getLast());
            }
        }));
        neoForgeSupportedMinecraftVersions.set(versions.map(list -> list.stream()
                .map(supportedVersion -> "[" + supportedVersion + "]")
                .collect(Collectors.joining(","))));
    }

    public void from(MinecraftSettings other) {
        minecraft.convention(other.minecraft);
        mixin.convention(other.mixin);
        mixinExtras.convention(other.mixinExtras);
        supportedMinecraftVersions.convention(other.supportedMinecraftVersions);
        neoForgeSupportedMinecraftVersions.convention(other.neoForgeSupportedMinecraftVersions);
        supportMinecraftHotfixes.convention(other.supportMinecraftHotfixes);
    }

    private static Optional<String> getMinecraftRelease(String version) {
        Matcher releaseMatcher = MINECRAFT_RELEASE_PATTERN.matcher(version);
        if (releaseMatcher.matches()) {
            return Optional.of(releaseMatcher.group(1) + "." + releaseMatcher.group(2));
        }
        return Optional.empty();
    }

    private static String bumpMinorMinecraftRelease(String version) {
        Matcher releaseMatcher = MINECRAFT_RELEASE_PATTERN.matcher(version);
        if (releaseMatcher.matches()) {
            String majorVersion = releaseMatcher.group(1);
            int minorVersion = Integer.parseInt(releaseMatcher.group(2));
            return majorVersion + "." + minorVersion;
        }
        throw new IllegalArgumentException("Unable to parse Minecraft release version " + version);
    }

    private static String normaliseMinecraftVersionForFabric(String version) {
        Matcher snapshotMatcher = MINECRAFT_SNAPSHOT_PATTERN.matcher(version);
        if (snapshotMatcher.matches()) {
            String semverType = switch (snapshotMatcher.group(2)) {
                case "snapshot" -> "alpha";
                default -> snapshotMatcher.group(2);
            };
            return snapshotMatcher.group(1) + "-" + semverType + "." + snapshotMatcher.group(3);
        }
        return version;
    }
}
