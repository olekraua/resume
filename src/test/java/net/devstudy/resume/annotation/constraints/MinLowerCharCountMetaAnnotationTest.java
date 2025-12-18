package net.devstudy.resume.annotation.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import net.devstudy.resume.validator.MinLowerCharCountConstraintValidator;

class MinLowerCharCountMetaAnnotationTest {

    @Test
    void hasExpectedConstraintAndRetentionConfiguration() {
        Constraint constraint = MinLowerCharCount.class.getAnnotation(Constraint.class);
        assertNotNull(constraint);
        assertEquals(1, constraint.validatedBy().length);
        assertEquals(MinLowerCharCountConstraintValidator.class, constraint.validatedBy()[0]);

        Retention retention = MinLowerCharCount.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void declaresDefaultAnnotationAttributes() throws Exception {
        Object defaultMinLowerChars = MinLowerCharCount.class.getMethod("value").getDefaultValue();
        assertTrue(defaultMinLowerChars instanceof Integer);
        assertEquals(1, defaultMinLowerChars);
        assertEquals("{MinLowerCharCount}", MinLowerCharCount.class.getMethod("message").getDefaultValue());
    }
}

