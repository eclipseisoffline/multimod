package xyz.eclipseisoffline.multimod;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;

public class MultiModGradlePlugin implements Plugin<Project> {
    private static final String VERSION = MultiModGradlePlugin.class.getPackage().getImplementationVersion();

    @Override
    public void apply(Project target) {
        target.getPlugins().apply(JavaLibraryPlugin.class);
        target.getExtensions().create("multimod", MultiModExtension.class, target);
        MultiModConfigurations.initializeConfigurations(target);
        if (target == target.getRootProject()) {
            target.getLogger().lifecycle("MultiMod: " + VERSION);
        }
    }
}
