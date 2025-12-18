package net.devstudy.resume.annotation.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import net.devstudy.resume.validator.AdulthoodConstraintValidator;

class AdulthoodMetaAnnotationTest {

    @Test
    void hasExpectedConstraintAndRetentionConfiguration() {
        Constraint constraint = Adulthood.class.getAnnotation(Constraint.class);
        assertNotNull(constraint);
        assertEquals(1, constraint.validatedBy().length);
        assertEquals(AdulthoodConstraintValidator.class, constraint.validatedBy()[0]);

        Retention retention = Adulthood.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void declaresDefaultAdulthoodAge() throws Exception {
        Object defaultAge = Adulthood.class.getMethod("adulthoodAge").getDefaultValue();
        assertTrue(defaultAge instanceof Integer);
        assertEquals(18, defaultAge);
    }
}

