package net.devstudy.resume.annotation.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import net.devstudy.resume.validator.MinUpperCharCountConstraintValidator;

class MinUpperCharCountMetaAnnotationTest {

    @Test
    void hasExpectedConstraintAndRetentionConfiguration() {
        Constraint constraint = MinUpperCharCount.class.getAnnotation(Constraint.class);
        assertNotNull(constraint);
        assertEquals(1, constraint.validatedBy().length);
        assertEquals(MinUpperCharCountConstraintValidator.class, constraint.validatedBy()[0]);

        Retention retention = MinUpperCharCount.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void declaresDefaultAnnotationAttributes() throws Exception {
        Object defaultMinUpperChars = MinUpperCharCount.class.getMethod("value").getDefaultValue();
        assertTrue(defaultMinUpperChars instanceof Integer);
        assertEquals(1, defaultMinUpperChars);
        assertEquals("{MinUpperCharCount}", MinUpperCharCount.class.getMethod("message").getDefaultValue());
    }
}

