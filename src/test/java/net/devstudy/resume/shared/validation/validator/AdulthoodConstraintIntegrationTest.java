package net.devstudy.resume.shared.validation.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.devstudy.resume.shared.validation.annotation.Adulthood;
import net.devstudy.resume.profile.form.InfoForm;

class AdulthoodConstraintIntegrationTest {

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
    void validatesAdulthoodAnnotationDefaultsOnInfoForm() throws Exception {
        Adulthood annotation = InfoForm.class.getDeclaredField("birthDay").getAnnotation(Adulthood.class);
        assertNotNull(annotation);
        assertEquals(18, annotation.adulthoodAge());
    }

    @Test
    void acceptsAdultBirthDayByDefault() {
        InfoForm form = new InfoForm();
        form.setObjective("objective");
        form.setSummary("summary");
        form.setBirthDay(Date.valueOf(LocalDate.now().minusYears(18).minusDays(10)));

        Set<ConstraintViolation<InfoForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsTooYoungBirthDayByDefault() {
        InfoForm form = new InfoForm();
        form.setObjective("objective");
        form.setSummary("summary");
        form.setBirthDay(Date.valueOf(LocalDate.now().minusYears(18).plusDays(10)));

        Set<ConstraintViolation<InfoForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        ConstraintViolation<InfoForm> violation = violations.iterator().next();
        assertEquals("birthDay", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof Adulthood);
    }
}
