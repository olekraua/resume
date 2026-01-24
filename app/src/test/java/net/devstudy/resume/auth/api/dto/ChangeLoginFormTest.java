package net.devstudy.resume.auth.api.dto;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

class ChangeLoginFormTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        factory.close();
    }

    @Test
    void validUidPassesValidation() {
        ChangeLoginForm form = new ChangeLoginForm();
        form.setNewUid("john_doe-123");

        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void nullUidFailsNotBlank() {
        ChangeLoginForm form = new ChangeLoginForm();
        form.setNewUid(null);

        Set<ConstraintViolation<ChangeLoginForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotBlank.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void tooShortUidFailsSize() {
        ChangeLoginForm form = new ChangeLoginForm();
        form.setNewUid("ab");

        Set<ConstraintViolation<ChangeLoginForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(Size.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void tooLongUidFailsSize() {
        ChangeLoginForm form = new ChangeLoginForm();
        form.setNewUid("a".repeat(65));

        Set<ConstraintViolation<ChangeLoginForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(Size.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void invalidCharactersFailPattern() {
        ChangeLoginForm form = new ChangeLoginForm();
        form.setNewUid("abc$");

        Set<ConstraintViolation<ChangeLoginForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(Pattern.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }
}
