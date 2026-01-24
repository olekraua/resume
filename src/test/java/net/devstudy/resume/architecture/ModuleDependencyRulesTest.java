package net.devstudy.resume.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

class ModuleDependencyRulesTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("net.devstudy.resume");

    private static final List<String> MODULES = List.of(
            "app",
            "web",
            "profile",
            "staticdata",
            "auth",
            "search",
            "media",
            "notification",
            "shared"
    );

    private static final Map<String, Set<String>> ALLOWED_DEPENDENCIES = Map.of(
            "app", Set.of("shared", "web", "profile", "staticdata", "auth", "search", "media", "notification"),
            "web", Set.of("profile", "staticdata", "auth", "search", "media", "notification", "shared"),
            "profile", Set.of("staticdata", "search", "media", "shared"),
            "auth", Set.of("profile", "notification", "shared"),
            "search", Set.of("profile", "shared"),
            "media", Set.of("shared"),
            "notification", Set.of("shared"),
            "staticdata", Set.of("shared"),
            "shared", Set.of()
    );

    @Test
    void allClassesAreMappedToModule() {
        List<String> unmapped = new ArrayList<>();
        for (JavaClass clazz : CLASSES) {
            if ("unknown".equals(moduleOf(clazz))) {
                unmapped.add(clazz.getFullName());
            }
        }
        assertTrue(unmapped.isEmpty(), "Unmapped classes: " + unmapped);
    }

    @Test
    void moduleDependencyRules() {
        for (String source : MODULES) {
            for (String target : MODULES) {
                if (source.equals(target)) {
                    continue;
                }
                if (ALLOWED_DEPENDENCIES.getOrDefault(source, Set.of()).contains(target)) {
                    continue;
                }
                ArchRule rule = noClasses()
                        .that(inModule(source))
                        .should()
                        .dependOnClassesThat(inModule(target))
                        .because("module " + source + " must not depend on " + target);
                rule.check(CLASSES);
            }
        }
    }

    @Test
    void modulesShouldNotAccessOtherModulesRepositories() {
        for (String source : MODULES) {
            for (String target : MODULES) {
                if (source.equals(target)) {
                    continue;
                }
                ArchRule rule = noClasses()
                        .that(inModule(source))
                        .should()
                        .accessClassesThat()
                        .resideInAPackage("net.devstudy.resume." + target + ".internal.repository..")
                        .because("module " + source + " must not access repositories of " + target);
                rule.check(CLASSES);
            }
        }
    }

    @Test
    void modulesShouldNotAccessOtherModulesInternalPackages() {
        for (String source : MODULES) {
            for (String target : MODULES) {
                if (source.equals(target)) {
                    continue;
                }
                ArchRule rule = noClasses()
                        .that(inModule(source))
                        .should()
                        .accessClassesThat()
                        .resideInAPackage("net.devstudy.resume." + target + ".internal..")
                        .because("module " + source + " must not access internal packages of " + target);
                rule.check(CLASSES);
            }
        }
    }

    private static DescribedPredicate<JavaClass> inModule(String module) {
        return new DescribedPredicate<>("module " + module) {
            @Override
            public boolean test(JavaClass input) {
                return module.equals(moduleOf(input));
            }
        };
    }

    // Package-based mapping for net.devstudy.resume.<module>.* packages.
    private static String moduleOf(JavaClass clazz) {
        String pkg = clazz.getPackageName();
        if ("net.devstudy.resume".equals(pkg)
                || pkg.equals("net.devstudy.resume.app")
                || pkg.startsWith("net.devstudy.resume.app.")) {
            return "app";
        }
        if (pkg.equals("net.devstudy.resume.web") || pkg.startsWith("net.devstudy.resume.web.")) {
            return "web";
        }
        if (pkg.equals("net.devstudy.resume.profile") || pkg.startsWith("net.devstudy.resume.profile.")) {
            return "profile";
        }
        if (pkg.equals("net.devstudy.resume.staticdata") || pkg.startsWith("net.devstudy.resume.staticdata.")) {
            return "staticdata";
        }
        if (pkg.equals("net.devstudy.resume.auth") || pkg.startsWith("net.devstudy.resume.auth.")) {
            return "auth";
        }
        if (pkg.equals("net.devstudy.resume.search") || pkg.startsWith("net.devstudy.resume.search.")) {
            return "search";
        }
        if (pkg.equals("net.devstudy.resume.media") || pkg.startsWith("net.devstudy.resume.media.")) {
            return "media";
        }
        if (pkg.equals("net.devstudy.resume.notification") || pkg.startsWith("net.devstudy.resume.notification.")) {
            return "notification";
        }
        if (pkg.equals("net.devstudy.resume.shared") || pkg.startsWith("net.devstudy.resume.shared.")) {
            return "shared";
        }
        return "unknown";
    }
}
