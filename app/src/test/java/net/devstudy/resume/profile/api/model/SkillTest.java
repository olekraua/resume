package net.devstudy.resume.profile.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SkillTest {

    @Test
    void equalsReturnsTrueForSameInstance() {
        Skill skill = buildSkill(1L, "backend", "java");

        assertTrue(skill.equals(skill));
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        Skill skill = buildSkill(1L, "backend", "java");

        assertFalse(skill.equals(new Object()));
    }

    @Test
    void equalsReturnsFalseForDifferentEntityTypeWithSameId() {
        Skill skill = buildSkill(1L, "backend", "java");
        Course other = new Course();
        other.setId(1L);

        assertFalse(skill.equals(other));
    }

    @Test
    void equalsReturnsTrueWhenAllFieldsEqual() {
        Skill first = buildSkill(1L, "backend", "java");
        Skill second = buildSkill(1L, "backend", "java");

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseWhenCategoryDiffers() {
        Skill first = buildSkill(1L, "backend", "java");
        Skill second = buildSkill(1L, "frontend", "java");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseWhenValueDiffers() {
        Skill first = buildSkill(1L, "backend", "java");
        Skill second = buildSkill(1L, "backend", "kotlin");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseWhenBothIdsNullButValuesDiffer() {
        Skill first = buildSkill(null, "backend", "java");
        Skill second = buildSkill(null, "backend", "kotlin");

        assertFalse(first.equals(second));
    }

    private Skill buildSkill(Long id, String category, String value) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setCategory(category);
        skill.setValue(value);
        return skill;
    }
}
