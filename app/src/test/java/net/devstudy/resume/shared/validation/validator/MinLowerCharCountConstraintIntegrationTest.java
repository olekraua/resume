package net.devstudy.resume.shared.validation.validator;

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
import net.devstudy.resume.shared.validation.annotation.MinLowerCharCount;

class MinLowerCharCountConstraintIntegrationTest {

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
    void rejectsWhenNotEnoughLowercaseLetters() {
        Bean bean = new Bean();
        bean.value = "ABC";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<Bean> violation = violations.iterator().next();
        assertEquals("value", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof MinLowerCharCount);
    }

    @Test
    void acceptsWhenEnoughLowercaseLetters() {
        Bean bean = new Bean();
        bean.value = "abC";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    private static class Bean {
        @MinLowerCharCount(2)
        String value;
    }
}

