package net.devstudy.resume.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.annotation.constraints.Adulthood;

class AdulthoodConstraintValidatorTest {

    @Test
    void acceptsNullValue() {
        AdulthoodConstraintValidator validator = new AdulthoodConstraintValidator();
        validator.initialize(adulthood(18));

        assertTrue(validator.isValid(null, null));
    }

    @Test
    void validatesDefaultAgeLogic() {
        AdulthoodConstraintValidator validator = new AdulthoodConstraintValidator();
        validator.initialize(adulthood(18));

        assertTrue(validator.isValid(Date.valueOf(LocalDate.now().minusYears(18).minusDays(10)), null));
        assertFalse(validator.isValid(Date.valueOf(LocalDate.now().minusYears(18).plusDays(10)), null));
    }

    @Test
    void respectsCustomAdulthoodAge() {
        AdulthoodConstraintValidator validator = new AdulthoodConstraintValidator();
        validator.initialize(adulthood(21));

        assertTrue(validator.isValid(Date.valueOf(LocalDate.now().minusYears(21).minusDays(10)), null));
        assertFalse(validator.isValid(Date.valueOf(LocalDate.now().minusYears(21).plusDays(10)), null));
    }

    private static Adulthood adulthood(int age) {
        return (Adulthood) Proxy.newProxyInstance(
                Adulthood.class.getClassLoader(),
                new Class<?>[] { Adulthood.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "adulthoodAge" -> age;
                    case "annotationType" -> Adulthood.class;
                    default -> method.getDefaultValue();
                });
    }
}

