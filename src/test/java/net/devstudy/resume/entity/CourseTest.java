package net.devstudy.resume.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CourseTest {

    @Test
    void equalsReturnsTrueForSameInstance() {
        Course course = buildCourse(1L, "Course", "School", LocalDate.of(2020, 1, 1));

        assertTrue(course.equals(course));
    }

    @Test
    void equalsReturnsFalseWhenIdsDiffer() {
        Course first = buildCourse(1L, "Course", "School", LocalDate.of(2020, 1, 1));
        Course second = buildCourse(2L, "Course", "School", LocalDate.of(2020, 1, 1));

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentAbstractEntityTypeWithSameId() {
        Course course = buildCourse(1L, "Course", "School", LocalDate.of(2020, 1, 1));
        OtherEntity other = new OtherEntity(1L);

        assertFalse(course.equals(other));
    }

    @Test
    void equalsReturnsTrueWhenAllFieldsEqual() {
        Course first = buildCourse(1L, "Course", "School", LocalDate.of(2020, 1, 1));
        Course second = buildCourse(1L, "Course", "School", LocalDate.of(2020, 1, 1));

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsFalseWhenFinishDateDiffers() {
        Course first = buildCourse(1L, "Course", "School", LocalDate.of(2020, 1, 1));
        Course second = buildCourse(1L, "Course", "School", LocalDate.of(2021, 1, 1));

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseWhenNameDiffers() {
        Course first = buildCourse(1L, "Course", "School", LocalDate.of(2020, 1, 1));
        Course second = buildCourse(1L, "Other", "School", LocalDate.of(2020, 1, 1));

        assertFalse(first.equals(second));
    }

    private static Course buildCourse(Long id, String name, String school, LocalDate finishDate) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setSchool(school);
        course.setFinishDate(finishDate);
        return course;
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
