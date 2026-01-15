package net.devstudy.resume.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class LayeringRulesTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("net.devstudy.resume");

    @Test
    void controllersShouldBeAnnotated() {
        classes()
                .that()
                .resideInAPackage("..controller..")
                .and()
                .areTopLevelClasses()
                .should()
                .beAnnotatedWith(Controller.class)
                .orShould()
                .beAnnotatedWith(RestController.class)
                .orShould()
                .beAnnotatedWith(ControllerAdvice.class)
                .check(CLASSES);
    }

    @Test
    void servicesShouldNotDependOnControllers() {
        noClasses()
                .that()
                .resideInAPackage("..service..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..controller..")
                .because("services should not depend on web layer")
                .check(CLASSES);
    }

    @Test
    void entitiesShouldNotDependOnWebOrServiceOrRepository() {
        noClasses()
                .that()
                .resideInAPackage("..entity..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..controller..", "..service..", "..repository..")
                .because("entities should stay in the domain layer")
                .check(CLASSES);
    }

    @Test
    void repositoriesShouldBeInterfaces() {
        classes()
                .that()
                .resideInAPackage("..repository..")
                .should()
                .beInterfaces()
                .check(CLASSES);
    }
}
