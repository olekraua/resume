package net.devstudy.resume.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.Test;

class AbstractEntityTest {

    @Test
    void equalsReturnsTrueForSameInstance() {
        TestEntity entity = new TestEntity(1L);

        assertTrue(entity.equals(entity));
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        TestEntity entity = new TestEntity(1L);

        assertFalse(entity.equals(new Object()));
    }

    @Test
    void equalsReturnsTrueForSameId() {
        TestEntity first = new TestEntity(2L);
        TestEntity second = new TestEntity(2L);

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentId() {
        TestEntity first = new TestEntity(2L);
        TestEntity second = new TestEntity(3L);

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsTrueWhenBothIdsNull() {
        TestEntity first = new TestEntity(null);
        TestEntity second = new TestEntity(null);

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(Objects.hash((Object) null), first.hashCode());
    }

    @Test
    void toStringUsesSimpleClassNameAndId() {
        TestEntity entity = new TestEntity(7L);

        assertEquals("TestEntity[id=7]", entity.toString());
    }

    private static final class TestEntity extends AbstractEntity<Long> {

        private final Long id;

        private TestEntity(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }
}
