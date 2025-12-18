package net.devstudy.resume.annotation.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

class PasswordStrengthMetaAnnotationTest {

    @Test
    void hasExpectedConstraintAndRetentionConfiguration() {
        Constraint constraint = PasswordStrength.class.getAnnotation(Constraint.class);
        assertNotNull(constraint);
        assertEquals(0, constraint.validatedBy().length);

        Retention retention = PasswordStrength.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void composesExpectedConstraints() {
        Size size = PasswordStrength.class.getAnnotation(Size.class);
        assertNotNull(size);
        assertEquals(8, size.min());

        NotBlank notBlank = PasswordStrength.class.getAnnotation(NotBlank.class);
        assertNotNull(notBlank);

        assertNotNull(PasswordStrength.class.getAnnotation(MinDigitCount.class));
        assertNotNull(PasswordStrength.class.getAnnotation(MinUpperCharCount.class));
        assertNotNull(PasswordStrength.class.getAnnotation(MinLowerCharCount.class));
        assertNotNull(PasswordStrength.class.getAnnotation(MinSpecCharCount.class));
    }
}

