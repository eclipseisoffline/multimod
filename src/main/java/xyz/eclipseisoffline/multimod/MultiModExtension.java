package xyz.eclipseisoffline.multimod;

import me.modmuss50.mpp.ReleaseType;
import net.fabricmc.loom.LoomGradlePlugin;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.neoforged.moddevgradle.boot.ModDevPlugin;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import net.neoforged.moddevgradle.dsl.RunModel;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MultiModExtension {
    public static final String MIXIN_VERSION = "0.8.7";
    public static final String MIXIN_EXTRAS_VERSION = "0.5.0";
    public static final String FABRIC_LOADER_VERSION = "0.17.2";

    private final Project target;

    private final Property<String> id;
    private final Property<String> name;
    private final Property<String> description;

    private final Property<String> archivesBaseName;

    private final Property<Dependency> minecraft;
    private final Property<String> neoFormTimestamp;
    private final Property<Dependency> parchment;

    private final Property<Dependency> mixin;
    private final Property<Dependency> mixinExtras;

    private final Property<Dependency> fabricLoader;
    private final Property<Dependency> fabricApi;

    private final Property<String> neoForgeVersion;
    private final Property<String> supportedNeoForgeVersions;

    private final Property<String> supportedMinecraftVersions;
    private final Property<String> neoForgeSupportedMinecraftVersions;

    private final Property<String> modrinthId;
    private final Property<ReleaseType> releaseType;
    private final Property<String> releaseVersions;
    private final Property<String> githubRepository;
    private final Property<String> gitBranch;

    private final ListProperty<MavenArtifactRepository> mavenRepositories;

    private final Property<Integer> targetJavaVersion;

    public MultiModExtension(@NotNull Project target) {
        this.target = target;

        ObjectFactory factory = target.getObjects();
        id = factory.property(String.class);
        name = factory.property(String.class);
        description = factory.property(String.class);

        archivesBaseName = factory.property(String.class);

        minecraft = factory.property(Dependency.class);
        neoFormTimestamp = factory.property(String.class);
        parchment = factory.property(Dependency.class);

        mixin = factory.property(Dependency.class);
        mixinExtras = factory.property(Dependency.class);

        fabricLoader = factory.property(Dependency.class);
        fabricApi = factory.property(Dependency.class);

        neoForgeVersion = factory.property(String.class);
        supportedNeoForgeVersions = factory.property(String.class);

        supportedMinecraftVersions = factory.property(String.class);
        neoForgeSupportedMinecraftVersions = factory.property(String.class);

        modrinthId = factory.property(String.class);
        releaseType = factory.property(ReleaseType.class);
        releaseVersions = factory.property(String.class);
        githubRepository = factory.property(String.class);
        gitBranch = factory.property(String.class);

        mavenRepositories = factory.listProperty(MavenArtifactRepository.class);

        targetJavaVersion = factory.property(Integer.class);

        mixin.convention(target.getDependencies().create("org.spongepowered:mixin:" + MIXIN_VERSION));
        mixinExtras.convention(target.getDependencies().create("io.github.llamalad7:mixinextras-common:" + MIXIN_EXTRAS_VERSION));

        fabricLoader.convention(target.getDependencies().create("net.fabricmc:fabric-loader:" + FABRIC_LOADER_VERSION));

        targetJavaVersion.convention(21);
    }

    public Property<String> getId() {
        return id;
    }

    public Property<String> getName() {
        return name;
    }

    public Property<String> getDescription() {
        return description;
    }

    public Property<String> getArchivesBaseName() {
        return archivesBaseName;
    }

    public Property<Dependency> getMinecraft() {
        return minecraft;
    }

    public Property<String> getNeoFormTimestamp() {
        return neoFormTimestamp;
    }

    public Property<Dependency> getParchment() {
        return parchment;
    }

    public Property<Dependency> getMixin() {
        return mixin;
    }

    public Property<Dependency> getMixinExtras() {
        return mixinExtras;
    }

    public Property<Dependency> getFabricLoader() {
        return fabricLoader;
    }

    public Property<Dependency> getFabricApi() {
        return fabricApi;
    }

    public Property<String> getNeoForgeVersion() {
        return neoForgeVersion;
    }

    public Property<String> getSupportedNeoForgeVersions() {
        return supportedNeoForgeVersions;
    }

    public Property<String> getSupportedMinecraftVersions() {
        return supportedMinecraftVersions;
    }

    public Property<String> getNeoForgeSupportedMinecraftVersions() {
        return neoForgeSupportedMinecraftVersions;
    }

    public Property<String> getModrinthId() {
        return modrinthId;
    }

    public Property<ReleaseType> getReleaseType() {
        return releaseType;
    }

    public Property<String> getReleaseVersions() {
        return releaseVersions;
    }

    public Property<String> getGithubRepository() {
        return githubRepository;
    }

    public Property<String> getGitBranch() {
        return gitBranch;
    }

    public ListProperty<MavenArtifactRepository> getMavenRepositories() {
        return mavenRepositories;
    }

    public Property<Integer> getTargetJavaVersion() {
        return targetJavaVersion;
    }

    private void baseConfiguration(@NotNull Project target, String type) {
        target.getPlugins().apply(JavaLibraryPlugin.class);

        target.getRepositories().mavenCentral();
        target.getRepositories().maven(maven -> {
            maven.setName("Mojang");
            maven.setUrl("https://libraries.minecraft.net");
        });
        target.getRepositories().maven(maven -> {
            maven.setName("ParchmentMC");
            maven.setUrl("https://maven.parchmentmc.org");
        });
        target.getRepositories().maven(maven -> {
            maven.setName("Fabric");
            maven.setUrl("https://maven.fabricmc.net");
        });
        target.getRepositories().maven(maven -> {
            maven.setName("NeoForged");
            maven.setUrl("https://maven.neoforged.net/releases");
        });

        BasePluginExtension baseExtension = target.getExtensions().getByType(BasePluginExtension.class);
        baseExtension.getArchivesName().set(archivesBaseName.map(s -> s + "-" + type));

        JavaPluginExtension javaExtension = target.getExtensions().getByType(JavaPluginExtension.class);
        javaExtension.withSourcesJar();

        JavaVersion javaVersion = JavaVersion.toVersion(targetJavaVersion.get());
        javaExtension.setSourceCompatibility(javaVersion);
        javaExtension.setTargetCompatibility(javaVersion);

        TaskContainer tasks = target.getTasks();
        tasks.withType(ProcessResources.class, resources -> {
            resources.getInputs().property("mod_id", id.getOrElse(""));
            resources.getInputs().property("mod_name", name.getOrElse(""));
            resources.getInputs().property("mod_description", description.getOrElse(""));
            resources.getInputs().property("version", target.getVersion());
            resources.getInputs().property("neoforge_version", supportedNeoForgeVersions.orElse(neoForgeVersion.map(version -> "[" + version + "]")).getOrElse(""));
            resources.getInputs().property("minecraft_version", supportedMinecraftVersions.orElse(minecraft.map(Dependency::getVersion)).getOrElse(""));
            resources.getInputs().property("neoforge_minecraft_version", neoForgeSupportedMinecraftVersions.orElse(minecraft.map(Dependency::getVersion).map(version -> "[" + version + "]")).getOrElse(""));
            resources.getInputs().property("loader_version", fabricLoader.map(Dependency::getVersion).getOrElse(""));
            resources.getInputs().property("fabric_api_version", fabricApi.map(Dependency::getVersion).getOrElse(""));
            resources.getInputs().property("modrinth_id", modrinthId.getOrElse(""));
            resources.getInputs().property("github_repository", githubRepository.getOrElse(""));

            resources.setFilteringCharset("UTF-8");

            resources.filesMatching(List.of("fabric.mod.json", "META-INF/neoforge.mods.toml"), details -> {
                details.expand(Map.ofEntries(
                        Map.entry("mod_id", id.getOrElse("")),
                        Map.entry("mod_name", name.getOrElse("")),
                        Map.entry("mod_description", description.getOrElse("")),
                        Map.entry("version", target.getVersion()),
                        Map.entry("neoforge_version", supportedNeoForgeVersions.orElse(neoForgeVersion.map(version -> "[" + version + "]")).getOrElse("")),
                        Map.entry("minecraft_version", supportedMinecraftVersions.orElse(minecraft.map(Dependency::getVersion)).getOrElse("")),
                        Map.entry("neoforge_minecraft_version", neoForgeSupportedMinecraftVersions.orElse(minecraft.map(Dependency::getVersion).map(version -> "[" + version + "]")).getOrElse("")),
                        Map.entry("loader_version", fabricLoader.map(Dependency::getVersion).getOrElse("")),
                        Map.entry("fabric_api_version", fabricApi.map(Dependency::getVersion).getOrElse("")),
                        Map.entry("modrinth_id", modrinthId.getOrElse("")),
                        Map.entry("github_repository", githubRepository.getOrElse(""))
                ));
            });
        });

        tasks.withType(JavaCompile.class, compile -> compile.getOptions().getRelease().set(targetJavaVersion));

        tasks.withType(Jar.class, jar -> {
            jar.getInputs().property("archivesName", baseExtension.getArchivesName());

            jar.from("LICENSE", copy -> copy.rename(license -> license + "_" + baseExtension.getArchivesName().get()));
        });
    }

    private static void includeProject(@NotNull Project target, @NotNull Project include) {
        target.getDependencies().add("compileOnly", include);

        JavaPluginExtension includeJava = include.getExtensions().getByType(JavaPluginExtension.class);
        includeJava.getSourceSets().forEach(set -> {
            target.getTasks().withType(JavaCompile.class, task -> task.source(set.getAllSource()));
            target.getTasks().named("sourcesJar", Jar.class, task -> task.from(set.getAllSource()));
            target.getTasks().withType(Javadoc.class, task -> task.source(set.getAllJava()));
            target.getTasks().withType(ProcessResources.class, task -> task.from(set.getResources()));
        });
    }

    public void common() {
        MultiModExtension rootExtension = target.getRootProject().getExtensions().getByType(MultiModExtension.class);
        rootExtension.baseConfiguration(target, "common");

        target.getPlugins().apply(ModDevPlugin.class);

        NeoForgeExtension neoForge = target.getExtensions().getByType(NeoForgeExtension.class);
        neoForge.setNeoFormVersion(rootExtension.minecraft.map(Dependency::getVersion).map(version -> version + "-" + rootExtension.neoFormTimestamp.get()).get());
        neoForge.parchment(configuration -> configuration.getParchmentArtifact().set(rootExtension.parchment.map(Object::toString)));

        target.getDependencies().add("compileOnly", rootExtension.mixin);
        target.getDependencies().add("compileOnly", rootExtension.mixinExtras);
    }

    public void fabric(@NotNull Project common) {
        MultiModExtension rootExtension = target.getRootProject().getExtensions().getByType(MultiModExtension.class);
        rootExtension.baseConfiguration(target, "fabric");

        target.getPlugins().apply(LoomGradlePlugin.class);

        LoomGradleExtensionAPI loom = target.getExtensions().getByType(LoomGradleExtensionAPI.class);

        DependencyHandler dependencies = target.getDependencies();
        dependencies.add("minecraft", rootExtension.minecraft);
        dependencies.add("mappings", loom.layered(layers -> {
            layers.officialMojangMappings();
            if (rootExtension.parchment.isPresent()) {
                layers.parchment(rootExtension.parchment);
            }
        }));
        dependencies.add("modImplementation", rootExtension.fabricLoader);

        if (rootExtension.fabricApi.isPresent()) {
            dependencies.add("modImplementation", rootExtension.fabricApi);
        }

        includeProject(target, common);
    }

    public void neoForge(@NotNull Project common) {
        MultiModExtension rootExtension = target.getRootProject().getExtensions().getByType(MultiModExtension.class);
        rootExtension.baseConfiguration(target, "neoforge");

        target.getPlugins().apply(ModDevPlugin.class);

        NeoForgeExtension neoForge = target.getExtensions().getByType(NeoForgeExtension.class);

        neoForge.setVersion(rootExtension.neoForgeVersion.get());
        neoForge.getValidateAccessTransformers().set(true);
        neoForge.parchment(configuration -> configuration.getParchmentArtifact().set(rootExtension.parchment.map(Object::toString)));

        neoForge.getRuns().register("client", RunModel::client);
        neoForge.getRuns().register("server", RunModel::server);

        JavaPluginExtension java = target.getExtensions().getByType(JavaPluginExtension.class);
        neoForge.getMods().register(rootExtension.id.get(), mod -> mod.sourceSet(java.getSourceSets().getByName("main")));

        target.getTasks().named("compileTestJava", task -> task.setEnabled(false));

        includeProject(target, common);
    }
}
