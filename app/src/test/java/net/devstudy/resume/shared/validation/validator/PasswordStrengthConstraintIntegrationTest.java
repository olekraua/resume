package net.devstudy.resume.shared.validation.validator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.devstudy.resume.shared.validation.annotation.MinDigitCount;
import net.devstudy.resume.shared.validation.annotation.MinLowerCharCount;
import net.devstudy.resume.shared.validation.annotation.MinSpecCharCount;
import net.devstudy.resume.shared.validation.annotation.MinUpperCharCount;
import net.devstudy.resume.shared.validation.annotation.PasswordStrength;

class PasswordStrengthConstraintIntegrationTest {

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
    void acceptsStrongPassword() {
        Bean bean = new Bean();
        bean.password = "Abcdef1!";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsWhenMissingDigitOrSpecialChar() {
        Bean bean = new Bean();
        bean.password = "Abcdefgh";

        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getConstraintDescriptor().getAnnotation() instanceof MinDigitCount));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getConstraintDescriptor().getAnnotation() instanceof MinSpecCharCount));
    }

    @Test
    void rejectsWhenMissingUppercaseOrLowercaseLetters() {
        Bean missingUppercase = new Bean();
        missingUppercase.password = "abcdef1!";
        Set<ConstraintViolation<Bean>> v1 = validator.validate(missingUppercase);
        assertTrue(v1.stream()
                .anyMatch(v -> v.getConstraintDescriptor().getAnnotation() instanceof MinUpperCharCount));

        Bean missingLowercase = new Bean();
        missingLowercase.password = "ABCDEF1!";
        Set<ConstraintViolation<Bean>> v2 = validator.validate(missingLowercase);
        assertTrue(v2.stream()
                .anyMatch(v -> v.getConstraintDescriptor().getAnnotation() instanceof MinLowerCharCount));
    }

    private static class Bean {
        @PasswordStrength
        String password;
    }
}

