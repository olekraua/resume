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
import net.devstudy.resume.annotation.constraints.RestoreIdentifier;

class RestoreIdentifierConstraintIntegrationTest {

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
    void acceptsNullAndBlank() {
        Bean bean = new Bean();
        bean.identifier = null;
        assertTrue(validator.validate(bean).isEmpty());

        bean.identifier = "";
        assertTrue(validator.validate(bean).isEmpty());

        bean.identifier = "   ";
        assertTrue(validator.validate(bean).isEmpty());
    }

    @Test
    void acceptsValidUid() {
        Bean bean = new Bean();
        bean.identifier = "john_doe-123";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    @Test
    void acceptsValidEmail() {
        Bean bean = new Bean();
        bean.identifier = "john.doe@example.com";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    @Test
    void acceptsValidPhone() {
        Bean bean = new Bean();
        bean.identifier = "+1 650-253-0000";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsInvalidIdentifier() {
        Bean bean = new Bean();
        bean.identifier = "ab@";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<Bean> violation = violations.iterator().next();
        assertEquals("identifier", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof RestoreIdentifier);
    }

    private static class Bean {
        @RestoreIdentifier
        String identifier;
    }
}
