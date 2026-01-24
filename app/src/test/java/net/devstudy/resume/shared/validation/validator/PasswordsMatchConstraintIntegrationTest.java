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
import net.devstudy.resume.shared.validation.annotation.PasswordsMatch;

class PasswordsMatchConstraintIntegrationTest {

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
    void rejectsWhenPasswordsDifferentAndViolationOnConfirmPassword() {
        Bean bean = new Bean();
        bean.setPassword("password123");
        bean.setConfirmPassword("password321");

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<Bean> violation = violations.iterator().next();
        assertEquals("confirmPassword", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof PasswordsMatch);
    }

    @Test
    void rejectsWhenBothBlankAndViolationOnConfirmPassword() {
        Bean bean = new Bean();
        bean.setPassword("   ");
        bean.setConfirmPassword("");

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<Bean> violation = violations.iterator().next();
        assertEquals("confirmPassword", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof PasswordsMatch);
    }

    @Test
    void acceptsWhenPasswordsMatch() {
        Bean bean = new Bean();
        bean.setPassword("password123");
        bean.setConfirmPassword("password123");

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    @PasswordsMatch
    private static class Bean {
        private String password;
        private String confirmPassword;

        @SuppressWarnings("unused")
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @SuppressWarnings("unused")
        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}

