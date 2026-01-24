package net.devstudy.resume.shared.validation.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import net.devstudy.resume.shared.validation.validator.PhoneConstraintValidator;

class PhoneMetaAnnotationTest {

    @Test
    void hasExpectedConstraintAndRetentionConfiguration() {
        Constraint constraint = Phone.class.getAnnotation(Constraint.class);
        assertNotNull(constraint);
        assertEquals(1, constraint.validatedBy().length);
        assertEquals(PhoneConstraintValidator.class, constraint.validatedBy()[0]);

        Retention retention = Phone.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void declaresDefaultAnnotationAttributes() throws Exception {
        assertEquals("{Phone}", Phone.class.getMethod("message").getDefaultValue());
    }
}

