package xyz.eclipseisoffline.multimod;

import me.modmuss50.mpp.ModPublishExtension;
import me.modmuss50.mpp.MppPlugin;
import me.modmuss50.mpp.PublishOptions;
import me.modmuss50.mpp.platforms.github.GithubOptions;
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions;
import net.fabricmc.loom.LoomNoRemapGradlePlugin;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.api.fabricapi.FabricApiExtension;
import net.neoforged.moddevgradle.boot.ModDevPlugin;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import net.neoforged.moddevgradle.dsl.RunModel;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.AbstractTestTask;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class MultiModExtension {
    private final Project target;

    public final MultiModSettings settings;

    public final Property<String> id;
    public final Property<String> name;
    public final Property<String> description;

    public final Property<String> archivesBaseName;

    public final MinecraftSettings minecraft;

    public final Property<Dependency> fabricLoader;
    public final Property<Dependency> fabricApi;

    public final Property<String> neoForgeVersion;
    public final Property<String> supportedNeoForgeVersions;

    public final Property<PublishOptions> modPublishOptions;
    public final Property<ModrinthOptions> modrinthOptions;
    public final Property<GithubOptions> githubOptions;

    public final Property<Integer> targetJavaVersion;

    public MultiModExtension(Project target) {
        this.target = target;

        ObjectFactory factory = target.getObjects();
        settings = new MultiModSettings(factory);

        id = factory.property(String.class);
        name = factory.property(String.class);
        description = factory.property(String.class);

        archivesBaseName = factory.property(String.class);

        minecraft = new MinecraftSettings(target);

        fabricLoader = factory.property(Dependency.class);
        fabricApi = factory.property(Dependency.class);

        neoForgeVersion = factory.property(String.class);
        supportedNeoForgeVersions = factory.property(String.class);

        modPublishOptions = factory.property(PublishOptions.class);
        modrinthOptions = factory.property(ModrinthOptions.class);
        githubOptions = factory.property(GithubOptions.class);

        targetJavaVersion = factory.property(Integer.class);

        name.convention(target.getName());
        description.convention("");
        archivesBaseName.convention(id);
        fabricLoader.convention(target.getDependencyFactory().create("net.fabricmc", "fabric-loader", MultiModVersions.FABRIC_LOADER_VERSION));
        supportedNeoForgeVersions.convention(neoForgeVersion.map(version -> "[" + version + "]"));
        targetJavaVersion.convention(MultiModVersions.JAVA_VERSION);
    }

    public void settings(Action<? super MultiModSettings> action) {
        action.execute(settings);
    }

    public void minecraft(String version) {
        minecraft(target.getDependencyFactory().create("com.mojang", "minecraft", version));
    }

    public void minecraft(Dependency dependency) {
        minecraft.minecraft.set(dependency);
    }

    public void minecraft(Provider<? extends Dependency> dependency) {
        minecraft.minecraft.set(dependency);
    }

    public void minecraft(Action<? super MinecraftSettings> action) {
        action.execute(minecraft);
    }

    private Provider<ModPublishExtension> modPublishProvider() {
        return target.provider(() -> target.getExtensions().findByType(ModPublishExtension.class));
    }

    public void modPublishOptions(Action<? super PublishOptions> action) {
        target.getPlugins().apply(MppPlugin.class);

        PublishOptions options = target.getObjects().newInstance(PublishOptions.class);
        action.execute(options);

        ModPublishExtension modPublish = target.getExtensions().getByType(ModPublishExtension.class);
        // Bit cursed, but we have to manually set all properties here instead of using .from, since .from
        // sets conventions instead of actual properties, which leads to overriding things we don't want to override
        // We also can't override the convention, so we have to check if an actual value was set with isPresent
        setIfPresent(modPublish.getFile(), options.getFile());
        setIfPresent(modPublish.getVersion(), options.getVersion());
        setIfPresent(modPublish.getChangelog(), options.getChangelog());
        setIfPresent(modPublish.getType(), options.getType());
        setIfPresent(modPublish.getDisplayName(), options.getDisplayName());
        setIfPresent(modPublish.getModLoaders(), options.getModLoaders());
        //setIfPresent(modPublish.getAdditionalFiles(), modPublishOptions.map(PublishOptions::getAdditionalFiles)); TODO
        setIfPresent(modPublish.getMaxRetries(), options.getMaxRetries());
    }

    public void modrinthOptions(Action<ModrinthOptions> action) {
        modrinthOptions.set(modPublishProvider().flatMap(modPublish -> modPublish.modrinthOptions(action)));
    }

    public void githubOptions(Action<GithubOptions> action) {
        githubOptions.set(modPublishProvider().flatMap(modPublish -> modPublish.githubOptions(action)));
    }

    public void publishToMaven(Action<? super MavenArtifactRepository> action) {
        target.getPlugins().apply(MavenPublishPlugin.class);
        PublishingExtension publishing = target.getExtensions().getByType(PublishingExtension.class);
        publishing.getRepositories().maven(action);
    }

    public void publishToMavenLocal() {
        target.getPlugins().apply(MavenPublishPlugin.class);
        target.getExtensions().getByType(PublishingExtension.class).getRepositories().mavenLocal();
    }

    private Provider<String> getArtifactId(String type) {
        return archivesBaseName.map(s -> s + "-" + type);
    }

    private void baseConfiguration(Project target, String type) {
        target.getPlugins().apply(JavaLibraryPlugin.class);
        settings.defaultRepositories.execute(target.getRepositories());

        BasePluginExtension baseExtension = target.getExtensions().getByType(BasePluginExtension.class);
        baseExtension.getArchivesName().set(getArtifactId(type));

        JavaPluginExtension javaExtension = target.getExtensions().getByType(JavaPluginExtension.class);
        if (settings.buildSourcesJar.get()) {
            javaExtension.withSourcesJar();
        }

        if (settings.setJavaVersion.get()) {
            JavaVersion javaVersion = JavaVersion.toVersion(targetJavaVersion.get());
            javaExtension.setSourceCompatibility(javaVersion);
            javaExtension.setTargetCompatibility(javaVersion);
        }

        TaskContainer tasks = target.getTasks();
        if (settings.configureResources.get()) {
            tasks.withType(ProcessResources.class, resources -> {
                resources.getInputs().property("mod_id", id.getOrElse(""));
                resources.getInputs().property("mod_name", name.getOrElse(""));
                resources.getInputs().property("mod_description", description.getOrElse(""));
                resources.getInputs().property("version", target.getVersion());
                resources.getInputs().property("minecraft_version", minecraft.supportedMinecraftVersions.getOrElse(""));
                resources.getInputs().property("neoforge_minecraft_version", minecraft.neoForgeSupportedMinecraftVersions.getOrElse(""));
                resources.getInputs().property("neoforge_version", supportedNeoForgeVersions.getOrElse(""));
                resources.getInputs().property("fabric_loader_version", fabricLoader.map(Dependency::getVersion).getOrElse(""));
                resources.getInputs().property("fabric_api_version", fabricApi.map(Dependency::getVersion).getOrElse(""));
                resources.getInputs().property("modrinth_id", modrinthOptions.flatMap(ModrinthOptions::getProjectId).getOrElse(""));
                resources.getInputs().property("github_repository", githubOptions.flatMap(GithubOptions::getRepository).getOrElse(""));

                resources.setFilteringCharset("UTF-8");

                resources.filesMatching(List.of("fabric.mod.json", "META-INF/neoforge.mods.toml"), details -> {
                    details.expand(Map.ofEntries(
                            Map.entry("mod_id", id.getOrElse("")),
                            Map.entry("mod_name", name.getOrElse("")),
                            Map.entry("mod_description", description.getOrElse("")),
                            Map.entry("version", target.getVersion()),
                            Map.entry("minecraft_version", minecraft.supportedMinecraftVersions.getOrElse("")),
                            Map.entry("neoforge_minecraft_version", minecraft.neoForgeSupportedMinecraftVersions.getOrElse("")),
                            Map.entry("neoforge_version", supportedNeoForgeVersions.getOrElse("")),
                            Map.entry("fabric_loader_version", fabricLoader.map(Dependency::getVersion).getOrElse("")),
                            Map.entry("fabric_api_version", fabricApi.map(Dependency::getVersion).getOrElse("")),
                            Map.entry("modrinth_id", modrinthOptions.flatMap(ModrinthOptions::getProjectId).getOrElse("")),
                            Map.entry("github_repository", githubOptions.flatMap(GithubOptions::getRepository).getOrElse(""))
                    ));
                });
            });
        }

        if (settings.setJavaVersion.get()) {
            tasks.withType(JavaCompile.class, compile -> compile.getOptions().getRelease().set(targetJavaVersion));
        }

        tasks.withType(Jar.class, jar -> {
            jar.getInputs().property("archivesName", baseExtension.getArchivesName().get());

            if (settings.includeLicenseInJar.get()) {
                jar.from("LICENSE", copy -> copy.rename(license -> license + "_" + baseExtension.getArchivesName().get()));
            }
        });
    }

    private void trySetupPublishing(Project subproject, String type, @Nullable String modLoader, @Nullable Provider<RegularFile> modFile) {
        PublishingExtension publishing = target.getExtensions().findByType(PublishingExtension.class);
        if (publishing != null) {
            publishing.getPublications().register(type, MavenPublication.class, publication -> {
                publication.setArtifactId(getArtifactId(type).get());
                publication.from(subproject.getComponents().getByName("java"));
            });
        }

        ModPublishExtension modPublish = target.getExtensions().findByType(ModPublishExtension.class);
        if (modPublish != null && modLoader != null && modFile != null) {
            if (modrinthOptions.isPresent()) {
                modPublish.modrinth("modrinth-" + type, modrinth -> {
                    modrinth.from(modrinthOptions.get());
                    modrinth.getFile().set(modFile);
                    modrinth.getModLoaders().add(modLoader);
                    modrinth.getDisplayName().convention(name.get() + "-" + type + " " + target.getVersion());
                });
            }
            if (githubOptions.isPresent()) {
                modPublish.github("github-" + type, github -> {
                    github.from(githubOptions.get());
                    github.getFile().set(modFile);
                    github.getDisplayName().convention(name.get() + "-" + type + " " + target.getVersion());
                    github.getTagName().convention(github.getVersion().map(version -> version + "-" + type));
                });
            }
        }
    }

    private static void includeProject(Project target, Project include) {
        target.getDependencies().add("compileOnly", include);

        JavaPluginExtension includeJava = include.getExtensions().getByType(JavaPluginExtension.class);
        includeJava.getSourceSets().forEach(set -> {
            target.getTasks().withType(JavaCompile.class, task -> task.source(set.getAllSource()));
            target.getTasks().named("sourcesJar", Jar.class, task -> task.from(set.getAllSource()));
            target.getTasks().withType(Javadoc.class, task -> task.source(set.getAllJava()));
            target.getTasks().withType(ProcessResources.class, task -> task.from(set.getResources()));
        });
        target.getTasks().withType(AbstractTestTask.class, task -> task.getFailOnNoDiscoveredTests().set(false));
    }

    public void common(Project root, Action<? super LoomGradleExtensionAPI> action) {
        MultiModExtension rootExtension = root.getExtensions().getByType(MultiModExtension.class);
        rootExtension.baseConfiguration(target, "common");

        target.getPlugins().apply(LoomNoRemapGradlePlugin.class);

        LoomGradleExtensionAPI loom = target.getExtensions().getByType(LoomGradleExtensionAPI.class);

        DependencyHandler dependencies = target.getDependencies();
        dependencies.add("minecraft", rootExtension.minecraft.minecraft);

        target.getDependencies().add("compileOnly", rootExtension.minecraft.mixin);
        target.getDependencies().add("compileOnly", rootExtension.minecraft.mixinExtras);

        loom.runs(Set::clear);

        rootExtension.trySetupPublishing(target, "common", null, null);

        action.execute(loom);
    }

    public void commonWithRoot(Project root) {
        common(root, loom -> {});
    }

    public void common(Action<? super LoomGradleExtensionAPI> action) {
        common(target.getRootProject(), action);
    }

    public void common() {
        common(loom -> {});
    }

    public void fabric(Project root, @Nullable Project common, Action<? super LoomAndFabricApiConfigurer> action) {
        MultiModExtension rootExtension = root.getExtensions().getByType(MultiModExtension.class);
        rootExtension.baseConfiguration(target, "fabric");

        target.getPlugins().apply(LoomNoRemapGradlePlugin.class);

        LoomGradleExtensionAPI loom = target.getExtensions().getByType(LoomGradleExtensionAPI.class);

        DependencyHandler dependencies = target.getDependencies();
        dependencies.add("minecraft", rootExtension.minecraft.minecraft);
        dependencies.add("implementation", rootExtension.fabricLoader);

        // TODO test removal of mixins ap

        if (rootExtension.fabricApi.isPresent()) {
            dependencies.add("implementation", rootExtension.fabricApi);
        }

        if (common != null) {
            includeProject(target, common);
        }
        rootExtension.trySetupPublishing(target, "fabric", "fabric", target.getTasks().named("jar", Jar.class).flatMap(Jar::getArchiveFile));

        FabricApiExtension fabricApi = target.getExtensions().getByType(FabricApiExtension.class);
        action.execute(new LoomAndFabricApiConfigurer() {
            @Override
            public void loom(Action<? super LoomGradleExtensionAPI> action) {
                action.execute(loom);
            }

            @Override
            public void fabricApi(Action<? super FabricApiExtension> action) {
                action.execute(fabricApi);
            }
        });
    }

    public void fabricWithRoot(Project root, @Nullable Project common) {
        fabric(root, common, configurer -> {});
    }

    public void fabricWithRoot(Project root, Action<? super LoomAndFabricApiConfigurer> action) {
        fabric(root, null, action);
    }

    public void fabricWithRoot(Project root) {
        fabric(root, null, configurer -> {});
    }

    public void fabric(@Nullable Project common, Action<? super LoomAndFabricApiConfigurer> action) {
        fabric(target.getRootProject(), common, action);
    }

    public void fabric(@Nullable Project common) {
        fabric(common, configurer -> {});
    }

    public void fabric(Action<? super LoomAndFabricApiConfigurer> action) {
        fabric(null, action);
    }

    public void fabric() {
        fabric(configurer -> {});
    }

    // TODO?
    public void neoForge(Project root, @Nullable Project common, Action<? super NeoForgeExtension> action) {
        MultiModExtension rootExtension = root.getExtensions().getByType(MultiModExtension.class);
        rootExtension.baseConfiguration(target, "neoforge");

        target.getPlugins().apply(ModDevPlugin.class);

        NeoForgeExtension neoForge = target.getExtensions().getByType(NeoForgeExtension.class);

        neoForge.enable(versionSettings -> {
            versionSettings.setVersion(rootExtension.neoForgeVersion.get());
            versionSettings.setDisableRecompilation(rootExtension.settings.disableNeoForgeRecompilation.get());
        });

        neoForge.getValidateAccessTransformers().set(true);

        neoForge.getRuns().register("client", RunModel::client);
        neoForge.getRuns().register("server", RunModel::server);

        JavaPluginExtension java = target.getExtensions().getByType(JavaPluginExtension.class);
        neoForge.getMods().register(rootExtension.id.get(), mod -> mod.sourceSet(java.getSourceSets().getByName("main")));

        if (common != null) {
            includeProject(target, common);
        }
        rootExtension.trySetupPublishing(target, "neoforge", "neoforge", target.getTasks().named("jar", Jar.class).flatMap(Jar::getArchiveFile));

        action.execute(neoForge);
    }

    public void neoForgeWithRoot(Project root, @Nullable Project common) {
        neoForge(root, common, neoForge -> {});
    }

    public void neoForgeWithRoot(Project root, Action<? super NeoForgeExtension> action) {
        neoForge(root, null, action);
    }

    public void neoForgeWithRoot(Project root) {
        neoForge(root, null, neoForge -> {});
    }

    public void neoForge(@Nullable Project common, Action<? super NeoForgeExtension> action) {
        neoForge(target.getRootProject(), common, action);
    }

    public void neoForge(@Nullable Project common) {
        neoForge(common, neoForge -> {});
    }

    public void neoForge(Action<? super NeoForgeExtension> action) {
        neoForge(null, action);
    }

    public void neoForge() {
        neoForge(neoForge -> {});
    }

    public interface LoomAndFabricApiConfigurer {

        void loom(Action<? super LoomGradleExtensionAPI> action);

        void fabricApi(Action<? super FabricApiExtension> action);
    }

    private static <T> void setIfPresent(Property<T> property, Property<T> other) {
        if (other.isPresent()) {
            property.set(other);
        }
    }

    private static <T> void setIfPresent(ListProperty<T> property, ListProperty<T> other) {
        if (other.isPresent()) {
            property.set(other);
        }
    }
}
