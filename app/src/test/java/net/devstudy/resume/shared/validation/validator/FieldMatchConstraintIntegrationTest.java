package net.devstudy.resume.shared.validation.validator;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.devstudy.resume.shared.validation.annotation.FieldMatch;
import net.devstudy.resume.auth.api.dto.PasswordForm;

class FieldMatchConstraintIntegrationTest {

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
    void rejectsWhenFieldsDoNotMatch() {
        PasswordForm form = new PasswordForm();
        form.setNewPassword("password1");
        form.setConfirmPassword("password2");

        Set<ConstraintViolation<PasswordForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        ConstraintViolation<PasswordForm> violation = violations.iterator().next();
        assertEquals("confirmPassword", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof FieldMatch);
    }

    @Test
    void acceptsWhenFieldsMatch() {
        PasswordForm form = new PasswordForm();
        form.setNewPassword("password1");
        form.setConfirmPassword("password1");

        Set<ConstraintViolation<PasswordForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void acceptsWhenBothFieldsNull() {
        SimpleBean bean = new SimpleBean();

        Set<ConstraintViolation<SimpleBean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
        assertEquals(null, bean.getFirst());
        assertEquals(null, bean.getSecond());
    }

    @Test
    void rejectsWhenFirstNullSecondHasValue() {
        SimpleBean bean = new SimpleBean();
        bean.setSecond("x");

        Set<ConstraintViolation<SimpleBean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<SimpleBean> violation = violations.iterator().next();
        assertEquals("second", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof FieldMatch);
    }

    @Test
    void acceptsWhenBothFieldsEqual() {
        SimpleBean bean = new SimpleBean();
        bean.setFirst("x");
        bean.setSecond("x");

        Set<ConstraintViolation<SimpleBean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsWhenFieldsDifferent() {
        SimpleBean bean = new SimpleBean();
        bean.setFirst("x");
        bean.setSecond("y");

        Set<ConstraintViolation<SimpleBean>> violations = validator.validate(bean);

        assertEquals(1, violations.size());
        ConstraintViolation<SimpleBean> violation = violations.iterator().next();
        assertEquals("second", violation.getPropertyPath().toString());
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof FieldMatch);
    }

    @Test
    void failsFastWhenConfiguredWithUnknownProperty() {
        BrokenBean bean = new BrokenBean();
        bean.setSecond("x");
        assertEquals("x", bean.getSecond());

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(bean));
        assertTrue(ex.getCause() != null);
    }

    @FieldMatch(first = "first", second = "second")
    private static class SimpleBean {
        private String first;
        private String second;

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getSecond() {
            return second;
        }

        public void setSecond(String second) {
            this.second = second;
        }
    }

    @FieldMatch(first = "missing", second = "second")
    private static class BrokenBean {
        private String second;

        public String getSecond() {
            return second;
        }

        public void setSecond(String second) {
            this.second = second;
        }
    }
}
