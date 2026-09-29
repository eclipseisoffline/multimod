package xyz.eclipseisoffline.multimod;

import me.modmuss50.mpp.platforms.github.GithubOptions;
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.util.HashMap;
import java.util.Map;

public final class MultiModResourceConfiguration {
    private final Project target;
    private final MultiModExtension extension;
    private final Map<String, Object> properties = new HashMap<>();
    public final ListProperty<String> filesToExpand;
    public final Property<String> filteringCharset;

    private boolean includedCommonDefaults = false;
    private boolean includedFabricDefaults = false;
    private boolean includedNeoForgeDefaults = false;

    MultiModResourceConfiguration(Project target, MultiModExtension extension, ObjectFactory factory) {
        this.target = target;
        this.extension = extension;
        filesToExpand = factory.listProperty(String.class);
        filteringCharset = factory.property(String.class);

        filteringCharset.convention("UTF-8");
    }

    public void expand(String reference, Object object) {
        properties.put(reference, object);
    }

    public void commonDefaults() {
        if (includedCommonDefaults) {
            return;
        }
        includedCommonDefaults = true;

        expand("mod_id", extension.id);
        expand("mod_name", extension.name);
        expand("mod_description", extension.description);
        expand("version", target.getVersion());
        expand("minecraft_version", extension.minecraft.supportedMinecraftVersions);
        expand("modrinth_id", extension.modPublishingSettings.resolveModrinthProperty(ModrinthOptions::getProjectId));
        expand("github_repository", extension.modPublishingSettings.resolveGithubProperty(GithubOptions::getRepository));
    }

    public void fabricDefaults() {
        if (includedFabricDefaults) {
            return;
        }
        includedFabricDefaults = true;

        commonDefaults();
        expand("fabric_loader_version", extension.fabricLoader.map(Dependency::getVersion));
        expand("fabric_api_version", extension.fabricApi.map(Dependency::getVersion));

        filesToExpand.add("fabric.mod.json");
    }

    public void neoForgeDefaults() {
        if (includedNeoForgeDefaults) {
            return;
        }
        includedNeoForgeDefaults = true;

        commonDefaults();
        expand("neoforge_minecraft_version", extension.minecraft.neoForgeSupportedMinecraftVersions);
        expand("neoforge_version", extension.supportedNeoForgeVersions);

        filesToExpand.add("META-INF/neoforge.mods.toml");
    }

    public void defaults() {
        fabricDefaults();
        neoForgeDefaults();
    }

    public void from(MultiModResourceConfiguration other) {
        properties.putAll(other.properties);
        filesToExpand.addAll(other.filesToExpand);
        filteringCharset.convention(other.filteringCharset);

        includedCommonDefaults = other.includedCommonDefaults;
        includedFabricDefaults = other.includedFabricDefaults;
        includedNeoForgeDefaults = other.includedNeoForgeDefaults;
    }

    void apply(ProcessResources resources) {
        String charset = filteringCharset.getOrNull();
        if (charset != null) {
            resources.setFilteringCharset(charset);
        }

        Map<String, Object> finalizedProperties = new HashMap<>();
        properties.forEach((reference, object) -> {
            if (object instanceof Provider<?> provider) {
                finalizedProperties.put(reference, ((Provider<Object>) provider).getOrElse(""));
            } else {
                finalizedProperties.put(reference, object);
            }
        });

        resources.filesMatching(filesToExpand.get(), details -> details.expand(finalizedProperties));
    }
}
