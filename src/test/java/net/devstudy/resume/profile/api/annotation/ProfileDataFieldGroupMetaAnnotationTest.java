package net.devstudy.resume.profile.api.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ProfileDataFieldGroupMetaAnnotationTest {

    @Test
    void hasRuntimeRetentionAndFieldTarget() {
        Retention retention = ProfileDataFieldGroup.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        Target target = ProfileDataFieldGroup.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertEquals(Set.of(ElementType.FIELD), Set.of(target.value()));
    }
}

