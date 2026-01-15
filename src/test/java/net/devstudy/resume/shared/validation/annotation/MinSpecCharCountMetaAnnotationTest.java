package net.devstudy.resume.shared.validation.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import net.devstudy.resume.shared.validation.validator.MinSpecCharCountConstraintValidator;

class MinSpecCharCountMetaAnnotationTest {

    @Test
    void hasExpectedConstraintAndRetentionConfiguration() {
        Constraint constraint = MinSpecCharCount.class.getAnnotation(Constraint.class);
        assertNotNull(constraint);
        assertEquals(1, constraint.validatedBy().length);
        assertEquals(MinSpecCharCountConstraintValidator.class, constraint.validatedBy()[0]);

        Retention retention = MinSpecCharCount.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void declaresDefaultAnnotationAttributes() throws Exception {
        Object defaultMinSpecChars = MinSpecCharCount.class.getMethod("value").getDefaultValue();
        assertTrue(defaultMinSpecChars instanceof Integer);
        assertEquals(1, defaultMinSpecChars);
        assertEquals("{MinSpecCharCount}", MinSpecCharCount.class.getMethod("message").getDefaultValue());
    }
}

