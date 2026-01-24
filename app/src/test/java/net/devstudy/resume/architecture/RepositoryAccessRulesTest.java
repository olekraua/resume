package net.devstudy.resume.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class RepositoryAccessRulesTest {

    @Test
    void controllersShouldNotAccessRepositoriesDirectly() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("net.devstudy.resume");

        noClasses()
                .that()
                .resideInAPackage("..controller..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("..repository..")
                .because("controllers should use services instead of repositories")
                .check(classes);
    }
}
