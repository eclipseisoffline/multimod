package xyz.eclipseisoffline.multimod;

import me.modmuss50.mpp.PublishOptions;
import me.modmuss50.mpp.platforms.github.GithubOptions;
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.Optional;
import java.util.function.Function;

public class ModPublishingSettings {
    private final Optional<ModPublishingSettings> parent;

    private final PublishOptions base;
    private final ModrinthOptions modrinth;
    private final GithubOptions github;

    private boolean shouldConfigureModrinth;
    private boolean shouldConfigureGithub;

    public ModPublishingSettings(ObjectFactory factory, Optional<ModPublishingSettings> parent) {
        this.parent = parent;
        base = factory.newInstance(PublishOptions.class);
        modrinth = factory.newInstance(ModrinthOptions.class);
        github = factory.newInstance(GithubOptions.class);
    }

    public void base(Action<? super PublishOptions> action) {
        action.execute(base);
    }

    public void modrinth(Action<? super ModrinthOptions> action) {
        action.execute(modrinth);
        shouldConfigureModrinth = true;
    }

    public void github(Action<? super GithubOptions> action) {
        action.execute(github);
        shouldConfigureGithub = true;
    }

    void resolveModrinthOptions(ModrinthOptions destination) {
        parent.ifPresent(p -> p.resolveModrinthOptions(destination));
        copyBasePublishOptions(base, destination);
        copyModrinthOptions(modrinth, destination);
    }

    void resolveGithubOptions(GithubOptions destination) {
        parent.ifPresent(p -> p.resolveGithubOptions(destination));
        copyBasePublishOptions(base, destination);
        copyGithubOptions(github, destination);
    }

    <T> Provider<T> resolveModrinthProperty(Function<ModrinthOptions, Provider<T>> extractor) {
        return resolveOptionsProperty(settings -> settings.modrinth, extractor);
    }

    <T> Provider<T> resolveGithubProperty(Function<GithubOptions, Provider<T>> extractor) {
        return resolveOptionsProperty(settings -> settings.github, extractor);
    }

    private <O, T> Provider<T> resolveOptionsProperty(Function<ModPublishingSettings, O> optionsExtractor, Function<O, Provider<T>> propertyExtractor) {
        O options = optionsExtractor.apply(this);
        Provider<T> property = propertyExtractor.apply(options);
        if (parent.isPresent()) {
            property = property.orElse(parent.get().resolveOptionsProperty(optionsExtractor, propertyExtractor));
        }
        return property;
    }

    boolean shouldConfigureModPublishing() {
        return shouldConfigureModrinth() || shouldConfigureGithub();
    }

    boolean shouldConfigureModrinth() {
        return shouldConfigureModrinth || parent.map(ModPublishingSettings::shouldConfigureModrinth).orElse(false);
    }

    boolean shouldConfigureGithub() {
        return shouldConfigureGithub || parent.map(ModPublishingSettings::shouldConfigureGithub).orElse(false);
    }

    private static void copyBasePublishOptions(PublishOptions source, PublishOptions destination) {
        // Bit cursed, but we have to manually set all properties here instead of using .from, since .from
        // sets conventions instead of actual properties, which leads to overriding things we don't want to override
        // We also can't override the convention, so we have to check if an actual value was set with isPresent
        setIfPresent(destination.getFile(), source.getFile());
        setIfPresent(destination.getVersion(), source.getVersion());
        setIfPresent(destination.getChangelog(), source.getChangelog());
        setIfPresent(destination.getType(), source.getType());
        setIfPresent(destination.getDisplayName(), source.getDisplayName());
        setIfPresent(destination.getModLoaders(), source.getModLoaders());
        //setIfPresent(destination.getAdditionalFiles(), source.map(PublishOptions::getAdditionalFiles)); TODO
        setIfPresent(destination.getMaxRetries(), source.getMaxRetries());
    }

    private static void copyModrinthOptions(ModrinthOptions source, ModrinthOptions destination) {
        copyBasePublishOptions(source, destination);
        setIfPresent(destination.getDependencies(), source.getDependencies());
        setIfPresent(destination.getProjectId(), source.getProjectId());
        setIfPresent(destination.getMinecraftVersions(), source.getMinecraftVersions());
        setIfPresent(destination.getFeatured(), source.getFeatured());
        setIfPresent(destination.getProjectDescription(), source.getProjectDescription());
        setIfPresent(destination.getApiEndpoint(), source.getApiEndpoint());
    }

    private static void copyGithubOptions(GithubOptions source, GithubOptions destination) {
        copyBasePublishOptions(source, destination);
        setIfPresent(destination.getRepository(), source.getRepository());
        setIfPresent(destination.getCommitish(), source.getCommitish());
        setIfPresent(destination.getTagName(), source.getTagName());
        setIfPresent(destination.getApiEndpoint(), source.getApiEndpoint());
        setIfPresent(destination.getAllowEmptyFiles(), source.getAllowEmptyFiles());
        setIfPresent(destination.getReleaseResult(), source.getReleaseResult());
    }

    private static <T> void setIfPresent(Property<T> destination, Property<T> source) {
        if (source.isPresent()) {
            destination.set(source);
        }
    }

    private static <T> void setIfPresent(ListProperty<T> destination, ListProperty<T> source) {
        if (source.isPresent()) {
            destination.set(source);
        }
    }
}
