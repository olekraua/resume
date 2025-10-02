package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import net.devstudy.resume.annotation.constraints.MinUpperCharCount;

/**
 * Validator for @MinUpperCharCount
 * Works with Java 21 + Spring Boot 3
 */
public class MinUpperCharCountConstraintValidator implements ConstraintValidator<MinUpperCharCount, CharSequence>  {

    private int minValue;

    @Override
    public void initialize(MinUpperCharCount constraintAnnotation) {
        minValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null вважається валідним
        }

        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isUpperCase(value.charAt(i))) {
                count++;
            }
        }
        return count >= minValue;
    }
}
