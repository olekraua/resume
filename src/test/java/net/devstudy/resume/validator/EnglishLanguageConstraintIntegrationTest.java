package net.devstudy.resume.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;

class EnglishLanguageConstraintIntegrationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    void validatesEnglishLanguageAnnotationViaJakartaValidator() {
        DefaultBean bean = new DefaultBean();
        bean.value = "Привіт";

        Set<ConstraintViolation<DefaultBean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<DefaultBean> violation = violations.iterator().next();
        assertEquals("value", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof EnglishLanguage);
    }

    @Test
    void respectsWithNumbersFlagViaJakartaValidator() {
        NoNumbersBean bean = new NoNumbersBean();
        bean.value = "Java 21";

        Set<ConstraintViolation<NoNumbersBean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<NoNumbersBean> violation = violations.iterator().next();
        assertEquals("value", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof EnglishLanguage);
    }

    private static class DefaultBean {
        @EnglishLanguage
        String value;
    }

    private static class NoNumbersBean {
        @EnglishLanguage(withNumbers = false)
        String value;
    }
}

