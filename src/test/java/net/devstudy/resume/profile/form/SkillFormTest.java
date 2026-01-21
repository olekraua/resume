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
import net.devstudy.resume.profile.entity.Skill;

class SkillFormTest {

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
        SkillForm form = new SkillForm();
        form.setItems(null);

        Set<ConstraintViolation<SkillForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void emptyItemsFailsNotEmpty() {
        SkillForm form = new SkillForm();
        form.setItems(List.of());

        Set<ConstraintViolation<SkillForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void itemsWithSkillPassValidation() {
        SkillForm form = new SkillForm();
        form.setItems(List.of(new Skill()));

        assertTrue(validator.validate(form).isEmpty());
    }
}
