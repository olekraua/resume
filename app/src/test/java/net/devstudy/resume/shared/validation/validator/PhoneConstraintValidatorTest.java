package net.devstudy.resume.shared.validation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.shared.validation.annotation.Phone;

class PhoneConstraintValidatorTest {

    private static class Holder {
        @Phone
        private String phone;
    }

    @Test
    void acceptsNullAndBlank() throws Exception {
        PhoneConstraintValidator validator = new PhoneConstraintValidator();
        validator.initialize(annotation());

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }

    @Test
    void rejectsInvalidPhoneNumber() throws Exception {
        PhoneConstraintValidator validator = new PhoneConstraintValidator();
        validator.initialize(annotation());

        assertFalse(validator.isValid("abc", null));
        assertFalse(validator.isValid("123", null));
    }

    @Test
    void acceptsValidInternationalPhoneNumber() throws Exception {
        PhoneConstraintValidator validator = new PhoneConstraintValidator();
        validator.initialize(annotation());

        assertTrue(validator.isValid("+1 650-253-0000", null));
        assertTrue(validator.isValid("  +1 650-253-0000  ", null));
    }

    private static Phone annotation() throws Exception {
        Phone annotation = Holder.class.getDeclaredField("phone").getAnnotation(Phone.class);
        if (annotation == null) {
            throw new IllegalStateException("@Phone not found on " + Holder.class.getName() + ".phone");
        }
        return annotation;
    }
}
