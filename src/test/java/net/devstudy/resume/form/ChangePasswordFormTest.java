package net.devstudy.resume.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import net.devstudy.resume.annotation.constraints.FieldMatch;

class ChangePasswordFormTest {

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
    void validFormPassesValidation() {
        ChangePasswordForm form = validForm();

        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void currentPasswordNullFailsNotBlank() {
        ChangePasswordForm form = validForm();
        form.setCurrentPassword(null);

        Set<ConstraintViolation<ChangePasswordForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        ConstraintViolation<ChangePasswordForm> violation = violations.iterator().next();
        assertEquals("currentPassword", violation.getPropertyPath().toString());
        assertEquals(NotBlank.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
    }

    @Test
    void newAndConfirmNullFailNotBlank() {
        ChangePasswordForm form = validForm();
        form.setNewPassword(null);
        form.setConfirmPassword(null);

        Set<ConstraintViolation<ChangePasswordForm>> violations = validator.validate(form);

        assertEquals(2, violations.size());
        assertEquals(Set.of("newPassword", "confirmPassword"),
                violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet()));
        assertEquals(Set.of(NotBlank.class),
                violations.stream()
                        .map(v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                        .collect(Collectors.toSet()));
    }

    @Test
    void newAndConfirmTooShortFailSize() {
        ChangePasswordForm form = validForm();
        form.setNewPassword("123");
        form.setConfirmPassword("123");

        Set<ConstraintViolation<ChangePasswordForm>> violations = validator.validate(form);

        assertEquals(2, violations.size());
        assertEquals(Set.of("newPassword", "confirmPassword"),
                violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet()));
        assertEquals(Set.of(Size.class),
                violations.stream()
                        .map(v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                        .collect(Collectors.toSet()));
    }

    @Test
    void mismatchTriggersFieldMatchOnConfirmPassword() {
        ChangePasswordForm form = validForm();
        form.setConfirmPassword("different123");

        Set<ConstraintViolation<ChangePasswordForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        ConstraintViolation<ChangePasswordForm> violation = violations.iterator().next();
        assertEquals("confirmPassword", violation.getPropertyPath().toString());
        assertEquals(FieldMatch.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
    }

    private ChangePasswordForm validForm() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("current123");
        form.setNewPassword("newpass123");
        form.setConfirmPassword("newpass123");
        return form;
    }
}
