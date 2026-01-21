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
import net.devstudy.resume.profile.entity.Education;

class EducationFormTest {

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
        EducationForm form = new EducationForm();
        form.setItems(null);

        Set<ConstraintViolation<EducationForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void emptyItemsFailsNotEmpty() {
        EducationForm form = new EducationForm();
        form.setItems(List.of());

        Set<ConstraintViolation<EducationForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals(NotEmpty.class, violations.iterator()
                .next()
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType());
    }

    @Test
    void itemsWithEducationPassValidation() {
        EducationForm form = new EducationForm();
        form.setItems(List.of(validEducation()));

        assertTrue(validator.validate(form).isEmpty());
    }

    private static Education validEducation() {
        Education education = new Education();
        education.setFaculty("Engineering");
        education.setSummary("Software Engineering");
        education.setUniversity("Kyiv Polytechnic Institute");
        education.setBeginYear(2020);
        return education;
    }
}
