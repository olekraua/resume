package net.devstudy.resume.profile.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import net.devstudy.resume.shared.model.AbstractEntity;

class EducationTest {

    @Test
    void isFinishReturnsFalseWhenFinishYearNull() {
        Education education = buildEducation(1L, "Faculty", "Summary", "University", 2020, null);

        assertFalse(education.isFinish());
    }

    @Test
    void isFinishReturnsTrueWhenFinishYearSet() {
        Education education = buildEducation(1L, "Faculty", "Summary", "University", 2020, 2022);

        assertTrue(education.isFinish());
    }

    @Test
    void equalsReturnsTrueForSameInstance() {
        Education education = buildEducation(1L, "Faculty", "Summary", "University", 2020, 2022);

        assertTrue(education.equals(education));
    }

    @Test
    void equalsReturnsFalseWhenIdsDiffer() {
        Education first = buildEducation(1L, "Faculty", "Summary", "University", 2020, 2022);
        Education second = buildEducation(2L, "Faculty", "Summary", "University", 2020, 2022);

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentAbstractEntityTypeWithSameId() {
        Education education = buildEducation(1L, "Faculty", "Summary", "University", 2020, 2022);
        OtherEntity other = new OtherEntity(1L);

        assertFalse(education.equals(other));
    }

    @Test
    void equalsReturnsTrueWhenAllFieldsEqual() {
        Education first = buildEducation(1L, "Faculty", "Summary", "University", 2020, 2022);
        Education second = buildEducation(1L, "Faculty", "Summary", "University", 2020, 2022);

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseWhenFieldDiffers() {
        Education first = buildEducation(1L, "Faculty", "Summary", "University", 2020, 2022);
        Education second = buildEducation(1L, "Other", "Summary", "University", 2020, 2022);

        assertFalse(first.equals(second));
    }

    private static Education buildEducation(Long id, String faculty, String summary, String university,
            Integer beginYear, Integer finishYear) {
        Education education = new Education();
        education.setId(id);
        education.setFaculty(faculty);
        education.setSummary(summary);
        education.setUniversity(university);
        education.setBeginYear(beginYear);
        education.setFinishYear(finishYear);
        return education;
    }

    private static final class OtherEntity extends AbstractEntity<Long> {

        private final Long id;

        private OtherEntity(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }
}
