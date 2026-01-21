package net.devstudy.resume.profile.form;

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

class HobbyFormTest {

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
        HobbyForm form = new HobbyForm();
        form.setHobbyIds(null);

        Set<ConstraintViolation<HobbyForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void emptyItemsFailsNotEmpty() {
        HobbyForm form = new HobbyForm();
        form.setHobbyIds(List.of());

        Set<ConstraintViolation<HobbyForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void itemsWithIdsPassValidation() {
        HobbyForm form = new HobbyForm();
        form.setHobbyIds(List.of(1L, 2L));

        assertTrue(validator.validate(form).isEmpty());
    }
}
