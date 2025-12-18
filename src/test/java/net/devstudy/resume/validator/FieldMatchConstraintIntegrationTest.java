package net.devstudy.resume.validator;

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
import net.devstudy.resume.annotation.constraints.FieldMatch;
import net.devstudy.resume.form.PasswordForm;

class FieldMatchConstraintIntegrationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    @SuppressWarnings("unused")
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    @SuppressWarnings("unused")
    static void tearDownValidator() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    void acceptsWhenFieldsMatch() {
        PasswordForm form = new PasswordForm();
        form.setNewPassword("password1");
        form.setConfirmPassword("password1");

        Set<ConstraintViolation<PasswordForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    @SuppressWarnings("unused")
    void acceptsWhenBothFieldsNull() {
        SimpleBean bean = new SimpleBean();

        Set<ConstraintViolation<SimpleBean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    @Test
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    void acceptsWhenBothFieldsEqual() {
        SimpleBean bean = new SimpleBean();
        bean.setFirst("x");
        bean.setSecond("x");

        Set<ConstraintViolation<SimpleBean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty());
    }

    @Test
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    void failsFastWhenConfiguredWithUnknownProperty() {
        BrokenBean bean = new BrokenBean();
        bean.setSecond("x");

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(bean));
        assertTrue(ex.getCause() != null);
    }

    @FieldMatch(first = "first", second = "second")
    private static class SimpleBean {
        private String first;
        private String second;

        @SuppressWarnings("unused")
        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
        public String getSecond() {
            return second;
        }

        public void setSecond(String second) {
            this.second = second;
        }
    }
}
