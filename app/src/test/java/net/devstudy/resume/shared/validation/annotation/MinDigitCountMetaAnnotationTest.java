package net.devstudy.resume.shared.validation.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import net.devstudy.resume.shared.validation.validator.MinDigitCountConstraintValidator;

class MinDigitCountMetaAnnotationTest {

    @Test
    void hasExpectedConstraintAndRetentionConfiguration() {
        Constraint constraint = MinDigitCount.class.getAnnotation(Constraint.class);
        assertNotNull(constraint);
        assertEquals(1, constraint.validatedBy().length);
        assertEquals(MinDigitCountConstraintValidator.class, constraint.validatedBy()[0]);

        Retention retention = MinDigitCount.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void declaresDefaultAnnotationAttributes() throws Exception {
        Object defaultMinDigits = MinDigitCount.class.getMethod("value").getDefaultValue();
        assertTrue(defaultMinDigits instanceof Integer);
        assertEquals(1, defaultMinDigits);
        assertEquals("{MinDigitCount}", MinDigitCount.class.getMethod("message").getDefaultValue());
    }
}

