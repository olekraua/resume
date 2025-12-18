package net.devstudy.resume.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.annotation.constraints.MinLowerCharCount;

class MinLowerCharCountConstraintValidatorTest {

    @SuppressWarnings("unused")
    private static class DefaultHolder {
        @MinLowerCharCount
        private String value;
    }

    @SuppressWarnings("unused")
    private static class TwoLowerCharsHolder {
        @MinLowerCharCount(2)
        private String value;
    }

    @Test
    void acceptsNullAndEmpty() throws Exception {
        MinLowerCharCountConstraintValidator validator = new MinLowerCharCountConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @Test
    void validatesDefaultMinLowerCharCount() throws Exception {
        MinLowerCharCountConstraintValidator validator = new MinLowerCharCountConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertFalse(validator.isValid("ABC", null));
        assertTrue(validator.isValid("AbC", null));
    }

    @Test
    void respectsCustomMinLowerCharCount() throws Exception {
        MinLowerCharCountConstraintValidator validator = new MinLowerCharCountConstraintValidator();
        validator.initialize(annotation(TwoLowerCharsHolder.class));

        assertFalse(validator.isValid("aBC", null));
        assertTrue(validator.isValid("abC", null));
    }

    private static MinLowerCharCount annotation(Class<?> holderType) throws Exception {
        MinLowerCharCount annotation = holderType.getDeclaredField("value").getAnnotation(MinLowerCharCount.class);
        if (annotation == null) {
            throw new IllegalStateException("@MinLowerCharCount not found on " + holderType.getName() + ".value");
        }
        return annotation;
    }
}

