package xyz.eclipseisoffline.multimod;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class MultiModGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        MultiModExtension extension = target.getExtensions().create("mod", MultiModExtension.class, target);
    }
}
