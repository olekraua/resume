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

    private static DescribedPredicate<JavaClass> inModule(String module) {
        return new DescribedPredicate<>("module " + module) {
            @Override
            public boolean test(JavaClass input) {
                return module.equals(moduleOf(input));
            }
        };
    }

    // Temporary mapping for the current package layout. Replace with package-based mapping
    // after refactoring to net.devstudy.resume.<module>.* packages.
    private static String moduleOf(JavaClass clazz) {
        String pkg = clazz.getPackageName();
        String name = clazz.getSimpleName();
        String baseName = name;
        String fullName = clazz.getFullName();
        int dollarIndex = fullName.indexOf('$');
        if (dollarIndex > 0) {
            int lastDot = fullName.lastIndexOf('.', dollarIndex);
            if (lastDot >= 0 && lastDot + 1 < dollarIndex) {
                baseName = fullName.substring(lastDot + 1, dollarIndex);
            }
        }

        if ("net.devstudy.resume".equals(pkg)) {
            return "app";
        }
        if (pkg.startsWith("net.devstudy.resume.shared")) {
            return "shared";
        }
        if (pkg.startsWith("net.devstudy.resume.util")) {
            return "SecurityUtil".equals(baseName) ? "auth" : "shared";
        }
        if (pkg.startsWith("net.devstudy.resume.controller") || pkg.startsWith("net.devstudy.resume.filter")) {
            return "web";
        }
        if (pkg.startsWith("net.devstudy.resume.config")) {
            if (Set.of("AppInfoConfig", "RepositoryConfig").contains(baseName)) {
                return "app";
            }
            if (Set.of("UiModelAttributes", "UiProperties", "UploadResourceConfig").contains(baseName)) {
                return "web";
            }
            if ("SecurityConfig".equals(baseName)) {
                return "auth";
            }
            if (baseName.startsWith("Elasticsearch")) {
                return "search";
            }
            if (Set.of("PhotoUploadProperties", "CertificateUploadProperties").contains(baseName)) {
                return "media";
            }
            if ("RestoreMailTemplateProperties".equals(baseName)) {
                return "notification";
            }
            return "app";
        }
        if (pkg.startsWith("net.devstudy.resume.search")) {
            return "search";
        }
        if (pkg.startsWith("net.devstudy.resume.mail")) {
            return "notification";
        }
        if (pkg.startsWith("net.devstudy.resume.event")) {
            if (baseName.startsWith("ProfileIndexing")) {
                return "search";
            }
            if (baseName.startsWith("RestoreAccessMail")) {
                return "notification";
            }
            return "shared";
        }
        if (pkg.startsWith("net.devstudy.resume.security")) {
            return "auth";
        }
        if (pkg.startsWith("net.devstudy.resume.component")) {
            if (Set.of("TemplateResolver", "FreemarkerTemplateResolver").contains(baseName)) {
                return "notification";
            }
            if (Set.of(
                    "PhotoFileStorage",
                    "CertificateFileStorage",
                    "ImageResizer",
                    "ImageOptimizator",
                    "ImageFormatConverter",
                    "UploadTempPathFactory",
                    "ThumbnailsImageResizer",
                    "JpegTranImageOptimizator",
                    "PngToJpegImageFormatConverter",
                    "DefaultUploadTempPathFactory",
                    "UploadImageTempStorage",
                    "UploadCertificateLinkTempStorage"
            ).contains(baseName)) {
                return "media";
            }
            if ("AccessDeniedHandlerImpl".equals(baseName)) {
                return "auth";
            }
            if ("FormErrorConverter".equals(baseName)) {
                return "shared";
            }
            return "shared";
        }
        if (pkg.startsWith("net.devstudy.resume.annotation")) {
            if ("EnableUploadImageTempStorage".equals(baseName)) {
                return "media";
            }
            if (Set.of("ProfileDataFieldGroup", "ProfileInfoField").contains(baseName)) {
                return "profile";
            }
            if ("EnableFormErrorConversion".equals(baseName)) {
                return "shared";
            }
            return "shared";
        }
        if (pkg.startsWith("net.devstudy.resume.entity")) {
            if (Set.of("SkillCategory", "Hobby").contains(baseName)) {
                return "staticdata";
            }
            if (Set.of("ProfileRestore", "RememberMeToken").contains(baseName)) {
                return "auth";
            }
            return "profile";
        }
        if (pkg.startsWith("net.devstudy.resume.repository.search")) {
            return "search";
        }
        if (pkg.startsWith("net.devstudy.resume.repository.storage")) {
            if (Set.of("SkillCategoryRepository", "HobbyRepository").contains(baseName)) {
                return "staticdata";
            }
            if (Set.of("ProfileRestoreRepository", "RememberMeTokenRepository").contains(baseName)) {
                return "auth";
            }
            return "profile";
        }
        if (pkg.startsWith("net.devstudy.resume.service")) {
            if (Set.of("ProfileService", "ProfileServiceImpl", "EditProfileService").contains(baseName)) {
                return "profile";
            }
            if (Set.of("StaticDataService", "StaticDataServiceImpl").contains(baseName)) {
                return "staticdata";
            }
            if (Set.of(
                    "RestoreAccessService",
                    "RestoreAccessServiceImpl",
                    "UidSuggestionService",
                    "UidSuggestionServiceImpl",
                    "CurrentProfileDetailsService",
                    "RememberMeService"
            ).contains(baseName)) {
                return "auth";
            }
            if (Set.of(
                    "ProfileSearchService",
                    "ProfileSearchServiceImpl",
                    "ProfileSearchServiceNoOp",
                    "ProfileSearchMapper",
                    "ProfileSearchMapperImpl"
            ).contains(baseName)) {
                return "search";
            }
            if (Set.of(
                    "PhotoStorageService",
                    "PhotoStorageServiceImpl",
                    "CertificateStorageService",
                    "CertificateStorageServiceImpl"
            ).contains(baseName)) {
                return "media";
            }
            if (Set.of(
                    "RestoreAccessMailService",
                    "RestoreAccessMailServiceImpl",
                    "RestoreAccessMailServiceNoOp"
            ).contains(baseName)) {
                return "notification";
            }
            return "shared";
        }
        if (pkg.startsWith("net.devstudy.resume.form")) {
            if (Set.of(
                    "RegistrationForm",
                    "ChangeLoginForm",
                    "ChangePasswordForm",
                    "RestoreAccessForm",
                    "RestorePasswordForm",
                    "PasswordForm",
                    "SignUpForm"
            ).contains(baseName)) {
                return "auth";
            }
            return "profile";
        }
        if (pkg.startsWith("net.devstudy.resume.model")) {
            if (Set.of("CurrentProfile", "CurrentProfileImpl").contains(baseName)) {
                return "auth";
            }
            if (Set.of("UploadTempPath", "UploadCertificateResult").contains(baseName)) {
                return "media";
            }
            return "shared";
        }
        if (pkg.startsWith("net.devstudy.resume.exception")) {
            return "UidAlreadyExistsException".equals(baseName) ? "profile" : "shared";
        }
        return "unknown";
    }
}
