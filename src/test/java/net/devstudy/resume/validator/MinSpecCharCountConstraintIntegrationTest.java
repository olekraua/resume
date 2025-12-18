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
import net.devstudy.resume.annotation.constraints.MinSpecCharCount;

class MinSpecCharCountConstraintIntegrationTest {

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
    void rejectsWhenNotEnoughSpecialCharacters() {
        Bean bean = new Bean();
        bean.value = "abc";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<Bean> violation = violations.iterator().next();
        assertEquals("value", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof MinSpecCharCount);
    }

    @Test
    void acceptsWhenEnoughSpecialCharacters() {
        Bean bean = new Bean();
        bean.value = "a@b#";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    private static class Bean {
        @MinSpecCharCount(value = 2, specSymbols = "@#")
        String value;
    }
}

