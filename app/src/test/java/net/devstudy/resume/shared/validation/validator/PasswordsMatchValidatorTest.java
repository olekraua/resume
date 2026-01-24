package net.devstudy.resume.shared.validation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.devstudy.resume.shared.validation.annotation.PasswordsMatch;
import net.devstudy.resume.auth.api.dto.RegistrationForm;

class PasswordsMatchValidatorTest {

    private PasswordsMatchValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordsMatchValidator();
        validator.initialize(RegistrationForm.class.getAnnotation(PasswordsMatch.class));
    }

    @Test
    void validWhenPasswordsMatch() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("password123");
        form.setConfirmPassword("password123");
        assertTrue(validator.isValid(form, null));
    }

    @Test
    void invalidWhenDifferent() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("password123");
        form.setConfirmPassword("password321");
        assertFalse(validator.isValid(form, null));
    }

    @Test
    void invalidWhenEmpty() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("");
        form.setConfirmPassword("");
        assertFalse(validator.isValid(form, null));
    }
}
