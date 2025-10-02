package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import net.devstudy.resume.annotation.constraints.MinDigitCount;

/**
 * Validator for @MinDigitCount
 */
public class MinDigitCountConstraintValidator implements ConstraintValidator<MinDigitCount, CharSequence>  {

    private int minValue;

    @Override
    public void initialize(MinDigitCount constraintAnnotation) {
        minValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null вважається валідним (можна змінити за потреби)
        }

        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                count++;
            }
        }
        return count >= minValue;
    }
}


