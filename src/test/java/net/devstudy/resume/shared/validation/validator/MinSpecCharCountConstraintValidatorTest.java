package net.devstudy.resume.shared.validation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.shared.validation.annotation.MinSpecCharCount;

class MinSpecCharCountConstraintValidatorTest {

    @SuppressWarnings("unused")
    private static class DefaultHolder {
        @MinSpecCharCount
        private String value;
    }

    @SuppressWarnings("unused")
    private static class TwoSpecCharsHolder {
        @MinSpecCharCount(2)
        private String value;
    }

    @SuppressWarnings("unused")
    private static class CustomSymbolsHolder {
        @MinSpecCharCount(value = 2, specSymbols = "@#")
        private String value;
    }

    @Test
    void acceptsNullAndEmpty() throws Exception {
        MinSpecCharCountConstraintValidator validator = new MinSpecCharCountConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @Test
    void validatesDefaultMinSpecCharCount() throws Exception {
        MinSpecCharCountConstraintValidator validator = new MinSpecCharCountConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertFalse(validator.isValid("abc", null));
        assertTrue(validator.isValid("a!b", null));
    }

    @Test
    void respectsCustomMinSpecCharCount() throws Exception {
        MinSpecCharCountConstraintValidator validator = new MinSpecCharCountConstraintValidator();
        validator.initialize(annotation(TwoSpecCharsHolder.class));

        assertFalse(validator.isValid("a!b", null));
        assertTrue(validator.isValid("a!b@", null));
    }

    @Test
    void respectsCustomSpecSymbols() throws Exception {
        MinSpecCharCountConstraintValidator validator = new MinSpecCharCountConstraintValidator();
        validator.initialize(annotation(CustomSymbolsHolder.class));

        assertFalse(validator.isValid("a@b!", null));
        assertTrue(validator.isValid("a@b#", null));
    }

    private static MinSpecCharCount annotation(Class<?> holderType) throws Exception {
        MinSpecCharCount annotation = holderType.getDeclaredField("value").getAnnotation(MinSpecCharCount.class);
        if (annotation == null) {
            throw new IllegalStateException("@MinSpecCharCount not found on " + holderType.getName() + ".value");
        }
        return annotation;
    }
}

