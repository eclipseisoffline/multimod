package xyz.eclipseisoffline.multimod;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.util.Objects;

public class MultiModDependencies {
    public final DomainObjectSet<Dependency> multiModApiSet;
    public final DomainObjectSet<Dependency> multiModImplementationSet;
    public final DomainObjectSet<Dependency> multiModCompileOnlySet;
    public final DomainObjectSet<Dependency> multiModCompileOnlyApiSet;
    public final DomainObjectSet<Dependency> multiModRuntimeOnlySet;
    public final DomainObjectSet<Dependency> multiModModIncludeSet;

    public MultiModDependencies(Project project) {
        ObjectFactory factory = project.getObjects();

        multiModApiSet = factory.domainObjectSet(Dependency.class);
        multiModImplementationSet = factory.domainObjectSet(Dependency.class);
        multiModCompileOnlySet = factory.domainObjectSet(Dependency.class);
        multiModCompileOnlyApiSet = factory.domainObjectSet(Dependency.class);
        multiModRuntimeOnlySet = factory.domainObjectSet(Dependency.class);
        multiModModIncludeSet = factory.domainObjectSet(Dependency.class);
    }

    public Provider<? extends Dependency> multiModApi(Provider<? extends Dependency> dependency) {
        multiModApiSet.addLater(dependency);
        return dependency;
    }

    public Provider<? extends Dependency> multiModImplementation(Provider<? extends Dependency> dependency) {
        multiModImplementationSet.addLater(dependency);
        return dependency;
    }

    public Provider<? extends Dependency> multiModCompileOnly(Provider<? extends Dependency> dependency) {
        multiModCompileOnlySet.addLater(dependency);
        return dependency;
    }

    public Provider<? extends Dependency> multiModCompileOnlyApi(Provider<? extends Dependency> dependency) {
        multiModCompileOnlyApiSet.addLater(dependency);
        return dependency;
    }

    public Provider<? extends Dependency> multiModRuntimeOnly(Provider<? extends Dependency> dependency) {
        multiModRuntimeOnlySet.addLater(dependency);
        return dependency;
    }

    public Provider<? extends Dependency> multiModInclude(Provider<? extends Dependency> dependency) {
        multiModModIncludeSet.addLater(dependency);
        return dependency;
    }

    public void from(Project project, MultiModDependencies other) {
        multiModApiSet.addAllLater(project.provider(() -> other.multiModApiSet));
        multiModImplementationSet.addAllLater(project.provider(() -> other.multiModImplementationSet));
        multiModCompileOnlySet.addAllLater(project.provider(() -> other.multiModCompileOnlySet));
        multiModCompileOnlyApiSet.addAllLater(project.provider(() -> other.multiModCompileOnlyApiSet));
        multiModRuntimeOnlySet.addAllLater(project.provider(() -> other.multiModRuntimeOnlySet));
        multiModModIncludeSet.addAllLater(project.provider(() -> other.multiModModIncludeSet));
    }

    public void apply(Project project, String type) {
        applyDependencySetToProject(project, multiModApiSet, "api", type);
        applyDependencySetToProject(project, multiModImplementationSet, "implementation", type);
        applyDependencySetToProject(project, multiModCompileOnlySet, "compileOnly", type);
        applyDependencySetToProject(project, multiModCompileOnlyApiSet, "compileOnlyApi", type);
        applyDependencySetToProject(project, multiModRuntimeOnlySet, "runtimeOnly", type);

        if (!type.equals("common")) {
            applyDependencySetToProject(project, multiModModIncludeSet, MultiModConfigurations.MOD_INCLUDE, type);
        }
    }

    private static void applyDependencySetToProject(Project project, DomainObjectSet<Dependency> set, String configuration, String type) {
        set.forEach(dependency -> {
            Objects.requireNonNull(project.getDependencies().add(configuration,
                            dependency.getGroup() + ":" + dependency.getName() + "-" + type + ":" + dependency.getVersion()))
                    .because(dependency.getReason() + " with type " + type);
        });
    }
}
