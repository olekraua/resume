package net.devstudy.resume.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.model.LanguageType;

class LanguageTest {

    @Test
    void equalsReturnsTrueForSameInstance() {
        Language language = new Language();

        assertTrue(language.equals(language));
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        Language language = new Language();

        assertFalse(language.equals(new Object()));
    }

    @Test
    void equalsReturnsTrueWhenBothIdsNull() {
        Language first = new Language();
        Language second = new Language();

        assertNotSame(first, second);
        assertEquals(first, second);
    }

    @Test
    void equalsReturnsTrueWhenIdsEqual() {
        Language first = new Language();
        first.setId(1L);
        Language second = new Language();
        second.setId(1L);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseWhenIdsDiffer() {
        Language first = new Language();
        first.setId(1L);
        Language second = new Language();
        second.setId(2L);

        assertFalse(first.equals(second));
    }

    @Test
    void isHasLanguageTypeReturnsFalseForAll() {
        Language language = new Language();
        language.setType(LanguageType.ALL);

        assertFalse(language.isHasLanguageType());
    }

    @Test
    void isHasLanguageTypeReturnsTrueForNonAll() {
        Language language = new Language();
        language.setType(LanguageType.SPOKEN);

        assertTrue(language.isHasLanguageType());
    }
}
