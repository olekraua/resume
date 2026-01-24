package net.devstudy.resume.profile.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class AbstractFinishDateEntityTest {

    @Test
    void isFinishReturnsFalseWhenFinishDateNull() {
        TestFinishEntity entity = new TestFinishEntity();

        assertFalse(entity.isFinish());
        assertNull(entity.getFinishDateMonth());
        assertNull(entity.getFinishDateYear());
    }

    @Test
    void isFinishReturnsTrueWhenFinishDateSet() {
        TestFinishEntity entity = new TestFinishEntity();
        entity.setFinishDate(LocalDate.of(2020, 5, 10));

        assertTrue(entity.isFinish());
        assertEquals(5, entity.getFinishDateMonth());
        assertEquals(2020, entity.getFinishDateYear());
    }

    @Test
    void setupFinishDateSetsDateWhenYearThenMonthProvided() {
        TestFinishEntity entity = new TestFinishEntity();

        entity.setFinishDateYear(2021);
        assertNull(entity.getFinishDate());

        entity.setFinishDateMonth(7);
        assertEquals(LocalDate.of(2021, 7, 1), entity.getFinishDate());
    }

    @Test
    void setupFinishDateSetsDateWhenMonthThenYearProvided() {
        TestFinishEntity entity = new TestFinishEntity();

        entity.setFinishDateMonth(12);
        assertNull(entity.getFinishDate());

        entity.setFinishDateYear(2022);
        assertEquals(LocalDate.of(2022, 12, 1), entity.getFinishDate());
    }

    @Test
    void setupFinishDateClearsWhenPartMissing() {
        TestFinishEntity entity = new TestFinishEntity();

        entity.setFinishDateYear(2023);
        assertNull(entity.getFinishDate());

        entity.setFinishDateMonth(1);
        assertEquals(LocalDate.of(2023, 1, 1), entity.getFinishDate());

        entity.setFinishDateMonth(null);
        assertNull(entity.getFinishDate());
    }

    private static final class TestFinishEntity extends AbstractFinishDateEntity<Long> {

        @Override
        public Long getId() {
            return 1L;
        }
    }
}
