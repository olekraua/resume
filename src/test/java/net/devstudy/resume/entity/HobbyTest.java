package net.devstudy.resume.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HobbyTest {

    @Test
    void cssClassNameReturnsEmptyWhenNameNull() {
        Hobby hobby = new Hobby();

        assertEquals("", hobby.getCssClassName());
    }

    @Test
    void cssClassNameNormalizesName() {
        Hobby hobby = new Hobby("Board Games");

        assertEquals("board-games", hobby.getCssClassName());
    }

    @Test
    void equalsReturnsTrueForSameInstance() {
        Hobby hobby = new Hobby("Chess");

        assertTrue(hobby.equals(hobby));
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        Hobby hobby = new Hobby("Chess");

        assertFalse(hobby.equals(new Object()));
    }

    @Test
    void equalsReturnsTrueWhenNamesEqual() {
        Hobby first = new Hobby("Chess");
        Hobby second = new Hobby("Chess");

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsTrueWhenBothNamesNull() {
        Hobby first = new Hobby();
        Hobby second = new Hobby();

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseWhenNamesDiffer() {
        Hobby first = new Hobby("Chess");
        Hobby second = new Hobby("Football");

        assertFalse(first.equals(second));
    }

    @Test
    void compareToReturnsPositiveWhenOtherNull() {
        Hobby hobby = new Hobby("Chess");

        assertEquals(1, hobby.compareTo(null));
    }

    @Test
    void compareToReturnsPositiveWhenThisNameNull() {
        Hobby hobby = new Hobby();
        Hobby other = new Hobby("Chess");

        assertEquals(1, hobby.compareTo(other));
    }

    @Test
    void compareToReturnsNegativeWhenOtherNameNull() {
        Hobby hobby = new Hobby("Chess");
        Hobby other = new Hobby();

        assertEquals(-1, hobby.compareTo(other));
    }

    @Test
    void compareToUsesNameComparison() {
        Hobby first = new Hobby("Chess");
        Hobby second = new Hobby("Football");

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void toStringUsesName() {
        Hobby hobby = new Hobby("Chess");

        assertEquals("Hobby[name=Chess]", hobby.toString());
    }
}
