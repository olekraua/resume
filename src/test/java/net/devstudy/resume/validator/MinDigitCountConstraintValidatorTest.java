package net.devstudy.resume.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.annotation.constraints.MinDigitCount;

class MinDigitCountConstraintValidatorTest {

    @SuppressWarnings("unused")
    private static class DefaultHolder {
        @MinDigitCount
        private String value;
    }

    @SuppressWarnings("unused")
    private static class TwoDigitsHolder {
        @MinDigitCount(2)
        private String value;
    }

    @Test
    void acceptsNullAndEmpty() throws Exception {
        MinDigitCountConstraintValidator validator = new MinDigitCountConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @Test
    void validatesDefaultMinDigitCount() throws Exception {
        MinDigitCountConstraintValidator validator = new MinDigitCountConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertFalse(validator.isValid("abc", null));
        assertTrue(validator.isValid("a1", null));
    }

    @Test
    void respectsCustomMinDigitCount() throws Exception {
        MinDigitCountConstraintValidator validator = new MinDigitCountConstraintValidator();
        validator.initialize(annotation(TwoDigitsHolder.class));

        assertFalse(validator.isValid("a1", null));
        assertTrue(validator.isValid("a1b2", null));
    }

    private static MinDigitCount annotation(Class<?> holderType) throws Exception {
        MinDigitCount annotation = holderType.getDeclaredField("value").getAnnotation(MinDigitCount.class);
        if (annotation == null) {
            throw new IllegalStateException("@MinDigitCount not found on " + holderType.getName() + ".value");
        }
        return annotation;
    }
}

