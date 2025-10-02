package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import net.devstudy.resume.annotation.constraints.MinSpecCharCount;

/**
 * Validator for @MinSpecCharCount
 * Works with Java 21 + Spring Boot 3
 */
public class MinSpecCharCountConstraintValidator implements ConstraintValidator<MinSpecCharCount, CharSequence>  {

    private int minValue;
    private String specSymbols;

    @Override
    public void initialize(MinSpecCharCount constraintAnnotation) {
        minValue = constraintAnnotation.value();
        specSymbols = constraintAnnotation.specSymbols();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null вважається валідним
        }

        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (specSymbols.indexOf(value.charAt(i)) != -1) {
                count++;
            }
        }
        return count >= minValue;
    }
}
