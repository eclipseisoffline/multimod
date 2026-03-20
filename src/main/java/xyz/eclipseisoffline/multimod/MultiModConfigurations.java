package xyz.eclipseisoffline.multimod;

import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;

public final class MultiModConfigurations {
    public static final String MOD_INCLUDE = "modInclude";

    private MultiModConfigurations() {}

    public static void initializeConfigurations(Project target) {
        target.getConfigurations().register(MOD_INCLUDE);
    }

    public static DependencySet getIncludeDependencies(Project target) {
        return target.getConfigurations().getByName(MOD_INCLUDE).getAllDependencies();
    }
}
