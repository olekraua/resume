package net.devstudy.resume.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import jakarta.validation.constraints.NotEmpty;
import net.devstudy.resume.entity.Language;

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
    void nullItemsFailsNotEmpty() {
        LanguageForm form = new LanguageForm();
        form.setItems(null);

        Set<ConstraintViolation<LanguageForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void emptyItemsFailsNotEmpty() {
        LanguageForm form = new LanguageForm();
        form.setItems(List.of());

        Set<ConstraintViolation<LanguageForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void itemsWithLanguagePassValidation() {
        LanguageForm form = new LanguageForm();
        form.setItems(List.of(new Language()));

        assertTrue(validator.validate(form).isEmpty());
    }
}
