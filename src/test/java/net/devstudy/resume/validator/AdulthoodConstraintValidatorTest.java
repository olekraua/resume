package net.devstudy.resume.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.annotation.constraints.Adulthood;

class AdulthoodConstraintValidatorTest {

    @SuppressWarnings("unused")
    private static class DefaultAgeHolder {
        @Adulthood
        private Date birthDay;
    }

    @SuppressWarnings("unused")
    private static class CustomAgeHolder {
        @Adulthood(adulthoodAge = 21)
        private Date birthDay;
    }

    @Test
    void acceptsNullValue() throws Exception {
        AdulthoodConstraintValidator validator = new AdulthoodConstraintValidator();
        validator.initialize(annotation(DefaultAgeHolder.class));

        assertTrue(validator.isValid(null, null));
    }

    @Test
    void validatesDefaultAgeLogic() throws Exception {
        AdulthoodConstraintValidator validator = new AdulthoodConstraintValidator();
        validator.initialize(annotation(DefaultAgeHolder.class));

        assertTrue(validator.isValid(Date.valueOf(LocalDate.now().minusYears(18).minusDays(10)), null));
        assertFalse(validator.isValid(Date.valueOf(LocalDate.now().minusYears(18).plusDays(10)), null));
    }

    @Test
    void respectsCustomAdulthoodAge() throws Exception {
        AdulthoodConstraintValidator validator = new AdulthoodConstraintValidator();
        validator.initialize(annotation(CustomAgeHolder.class));

        assertTrue(validator.isValid(Date.valueOf(LocalDate.now().minusYears(21).minusDays(10)), null));
        assertFalse(validator.isValid(Date.valueOf(LocalDate.now().minusYears(21).plusDays(10)), null));
    }

    private static Adulthood annotation(Class<?> holderType) throws Exception {
        Adulthood annotation = holderType.getDeclaredField("birthDay").getAnnotation(Adulthood.class);
        if (annotation == null) {
            throw new IllegalStateException("@Adulthood not found on " + holderType.getName() + ".birthDay");
        }
        return annotation;
    }
}
