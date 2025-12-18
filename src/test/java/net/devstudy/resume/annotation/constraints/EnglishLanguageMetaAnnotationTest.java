package net.devstudy.resume.annotation.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import net.devstudy.resume.validator.EnglishLanguageConstraintValidator;

class EnglishLanguageMetaAnnotationTest {

    @Test
    void hasExpectedConstraintAndRetentionConfiguration() {
        Constraint constraint = EnglishLanguage.class.getAnnotation(Constraint.class);
        assertNotNull(constraint);
        assertEquals(1, constraint.validatedBy().length);
        assertEquals(EnglishLanguageConstraintValidator.class, constraint.validatedBy()[0]);

        Retention retention = EnglishLanguage.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void declaresDefaultFlags() throws Exception {
        assertEquals(Boolean.TRUE, EnglishLanguage.class.getMethod("withPunctuations").getDefaultValue());
        assertEquals(Boolean.TRUE, EnglishLanguage.class.getMethod("withNumbers").getDefaultValue());
    }
}

