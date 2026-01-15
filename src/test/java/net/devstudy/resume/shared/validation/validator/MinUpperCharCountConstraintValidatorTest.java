package net.devstudy.resume.shared.validation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.shared.validation.annotation.MinUpperCharCount;

class MinUpperCharCountConstraintValidatorTest {

    @SuppressWarnings("unused")
    private static class DefaultHolder {
        @MinUpperCharCount
        private String value;
    }

    @SuppressWarnings("unused")
    private static class TwoUpperCharsHolder {
        @MinUpperCharCount(2)
        private String value;
    }

    @Test
    void acceptsNullAndEmpty() throws Exception {
        MinUpperCharCountConstraintValidator validator = new MinUpperCharCountConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @Test
    void validatesDefaultMinUpperCharCount() throws Exception {
        MinUpperCharCountConstraintValidator validator = new MinUpperCharCountConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertFalse(validator.isValid("abc", null));
        assertTrue(validator.isValid("abC", null));
    }

    @Test
    void respectsCustomMinUpperCharCount() throws Exception {
        MinUpperCharCountConstraintValidator validator = new MinUpperCharCountConstraintValidator();
        validator.initialize(annotation(TwoUpperCharsHolder.class));

        assertFalse(validator.isValid("aBc", null));
        assertTrue(validator.isValid("aBC", null));
    }

    private static MinUpperCharCount annotation(Class<?> holderType) throws Exception {
        MinUpperCharCount annotation = holderType.getDeclaredField("value").getAnnotation(MinUpperCharCount.class);
        if (annotation == null) {
            throw new IllegalStateException("@MinUpperCharCount not found on " + holderType.getName() + ".value");
        }
        return annotation;
    }
}
