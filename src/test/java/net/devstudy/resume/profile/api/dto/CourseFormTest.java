package net.devstudy.resume.profile.api.dto;

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
import net.devstudy.resume.profile.api.model.Course;

class CourseFormTest {

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
        CourseForm form = new CourseForm();
        form.setItems(null);

        Set<ConstraintViolation<CourseForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void emptyItemsFailsNotEmpty() {
        CourseForm form = new CourseForm();
        form.setItems(List.of());

        Set<ConstraintViolation<CourseForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void itemsWithCoursePassValidation() {
        CourseForm form = new CourseForm();
        form.setItems(List.of(new Course()));

        assertTrue(validator.validate(form).isEmpty());
    }
}
