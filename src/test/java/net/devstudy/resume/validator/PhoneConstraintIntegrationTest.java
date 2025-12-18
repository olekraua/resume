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
import net.devstudy.resume.annotation.constraints.Phone;

class PhoneConstraintIntegrationTest {

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
        bean.phone = null;
        assertTrue(validator.validate(bean).isEmpty());

        bean.phone = "";
        assertTrue(validator.validate(bean).isEmpty());

        bean.phone = "   ";
        assertTrue(validator.validate(bean).isEmpty());
    }

    @Test
    void rejectsInvalidPhoneNumber() {
        Bean bean = new Bean();
        bean.phone = "abc";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<Bean> violation = violations.iterator().next();
        assertEquals("phone", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof Phone);
    }

    @Test
    void acceptsValidPhoneNumber() {
        Bean bean = new Bean();
        bean.phone = "+1 650-253-0000";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    private static class Bean {
        @Phone
        String phone;
    }
}

