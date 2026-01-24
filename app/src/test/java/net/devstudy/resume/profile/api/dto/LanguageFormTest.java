package net.devstudy.resume.profile.api.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.devstudy.resume.profile.api.model.Language;

class LanguageFormTest {

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
    void nullItemsPassValidation() {
        LanguageForm form = new LanguageForm();
        form.setItems(null);

        Set<ConstraintViolation<LanguageForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void emptyItemsPassValidation() {
        LanguageForm form = new LanguageForm();
        form.setItems(List.of());

        Set<ConstraintViolation<LanguageForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void itemsWithLanguagePassValidation() {
        LanguageForm form = new LanguageForm();
        form.setItems(List.of(new Language()));

        assertTrue(validator.validate(form).isEmpty());
    }
}
