package net.devstudy.resume.shared.validation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.shared.validation.annotation.EnglishLanguage;

class EnglishLanguageConstraintValidatorTest {

    private static class DefaultHolder {
        @EnglishLanguage
        private String value;
    }

    private static class NoNumbersHolder {
        @EnglishLanguage(withNumbers = false)
        private String value;
    }

    private static class NoPunctuationsHolder {
        @EnglishLanguage(withPunctuations = false)
        private String value;
    }

    @Test
    void acceptsNullAndEmpty() throws Exception {
        EnglishLanguageConstraintValidator validator = new EnglishLanguageConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @Test
    void validatesWithDefaultFlags() throws Exception {
        EnglishLanguageConstraintValidator validator = new EnglishLanguageConstraintValidator();
        validator.initialize(annotation(DefaultHolder.class));

        assertTrue(validator.isValid("Hello world", null));
        assertTrue(validator.isValid("Hello-world", null));
        assertTrue(validator.isValid("Hello, world!", null));
        assertTrue(validator.isValid("Java 21", null));

        assertFalse(validator.isValid("Привіт", null));
        assertFalse(validator.isValid("café", null));
        assertFalse(validator.isValid("hello@", null));
    }

    @Test
    void rejectsNumbersWhenDisabled() throws Exception {
        EnglishLanguageConstraintValidator validator = new EnglishLanguageConstraintValidator();
        validator.initialize(annotation(NoNumbersHolder.class));

        assertTrue(validator.isValid("Java", null));
        assertFalse(validator.isValid("Java 21", null));
    }

    @Test
    void rejectsPunctuationsWhenDisabled() throws Exception {
        EnglishLanguageConstraintValidator validator = new EnglishLanguageConstraintValidator();
        validator.initialize(annotation(NoPunctuationsHolder.class));

        assertTrue(validator.isValid("Hello world", null));
        assertFalse(validator.isValid("Hello, world!", null));
    }

    private static EnglishLanguage annotation(Class<?> holderType) throws Exception {
        EnglishLanguage annotation = holderType.getDeclaredField("value").getAnnotation(EnglishLanguage.class);
        if (annotation == null) {
            throw new IllegalStateException("@EnglishLanguage not found on " + holderType.getName() + ".value");
        }
        return annotation;
    }
}
