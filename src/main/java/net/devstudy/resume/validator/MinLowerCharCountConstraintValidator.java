package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import net.devstudy.resume.annotation.constraints.MinLowerCharCount;

/**
 * Validator for @MinLowerCharCount
 * Works with Java 21 + Spring Boot 3
 */
public class MinLowerCharCountConstraintValidator implements ConstraintValidator<MinLowerCharCount, CharSequence>  {

    private int minValue;

    @Override
    public void initialize(MinLowerCharCount constraintAnnotation) {
        minValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null вважається валідним, як у стандартних Bean Validation
        }

        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLowerCase(value.charAt(i))) {
                count++;
            }
        }
        return count >= minValue;
    }
}

