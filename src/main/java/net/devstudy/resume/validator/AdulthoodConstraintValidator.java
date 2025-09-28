package net.devstudy.resume.validator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.devstudy.resume.annotation.constraints.Adulthood;

public class AdulthoodConstraintValidator implements ConstraintValidator<Adulthood, Date> {
    private int adulthoodAge;

    @Override
    public void initialize(Adulthood constraintAnnotation) {
        this.adulthoodAge = constraintAnnotation.adulthoodAge();
    }

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        LocalDate birthDate = value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate cutoff = LocalDate.now().minusYears(adulthoodAge);

        return !birthDate.isAfter(cutoff);
    }

}
