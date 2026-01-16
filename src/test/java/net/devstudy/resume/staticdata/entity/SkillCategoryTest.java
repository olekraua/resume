package net.devstudy.resume.staticdata.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SkillCategoryTest {

    @Test
    void gettersAndSettersWork() {
        SkillCategory category = new SkillCategory();
        category.setId(10L);
        category.setCategory("backend");

        assertEquals(10L, category.getId());
        assertEquals("backend", category.getCategory());
    }

    @Test
    void equalsReturnsTrueForSameInstance() {
        SkillCategory category = new SkillCategory();

        assertTrue(category.equals(category));
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        SkillCategory category = new SkillCategory();

        assertFalse(category.equals(new Object()));
    }

    @Test
    void equalsReturnsTrueWhenIdsEqual() {
        SkillCategory first = new SkillCategory();
        first.setId(1L);
        SkillCategory second = new SkillCategory();
        second.setId(1L);

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseWhenIdsDiffer() {
        SkillCategory first = new SkillCategory();
        first.setId(1L);
        SkillCategory second = new SkillCategory();
        second.setId(2L);

        assertFalse(first.equals(second));
    }
}
