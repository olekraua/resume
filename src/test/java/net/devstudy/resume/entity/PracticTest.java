package net.devstudy.resume.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PracticTest {

    @Test
    void equalsReturnsTrueForSameInstance() {
        Practic practic = buildPractic(1L);

        assertTrue(practic.equals(practic));
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        Practic practic = buildPractic(1L);

        assertFalse(practic.equals(new Object()));
    }

    @Test
    void equalsReturnsFalseForDifferentEntityTypeWithSameId() {
        Practic practic = buildPractic(1L);
        Course other = new Course();
        other.setId(1L);

        assertFalse(practic.equals(other));
    }

    @Test
    void equalsReturnsTrueWhenAllFieldsEqual() {
        Practic first = buildPractic(1L);
        Practic second = buildPractic(1L);

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseWhenCompanyDiffers() {
        Practic first = buildPractic(1L);
        Practic second = buildPractic(1L);
        second.setCompany("Other Corp");

        assertFalse(first.equals(second));
    }

    @Test
    void beginDateMonthYearDerivedFromBeginDate() {
        Practic practic = new Practic();
        practic.setBeginDate(LocalDate.of(2024, 7, 10));

        assertEquals(7, practic.getBeginDateMonth());
        assertEquals(2024, practic.getBeginDateYear());
    }

    @Test
    void beginDateIsNullUntilBothMonthAndYearSet() {
        Practic practic = new Practic();
        practic.setBeginDateYear(2024);

        assertNull(practic.getBeginDate());

        practic.setBeginDateMonth(3);

        assertEquals(LocalDate.of(2024, 3, 1), practic.getBeginDate());

        practic.setBeginDateMonth(null);

        assertNull(practic.getBeginDate());
    }

    private Practic buildPractic(Long id) {
        Practic practic = new Practic();
        practic.setId(id);
        practic.setCompany("Example Corp");
        practic.setDemo("https://demo.example");
        practic.setSrc("https://repo.example");
        practic.setPosition("Engineer");
        practic.setResponsibilities("Build stuff");
        practic.setBeginDate(LocalDate.of(2020, 1, 1));
        practic.setFinishDate(LocalDate.of(2021, 1, 1));
        return practic;
    }
}
