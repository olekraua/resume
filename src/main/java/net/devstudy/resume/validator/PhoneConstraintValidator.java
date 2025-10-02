package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import net.devstudy.resume.annotation.constraints.Phone;

/**
 * Validator for @Phone annotation
 * Works with Java 21 + Spring Boot 3
 */
public class PhoneConstraintValidator implements ConstraintValidator<Phone, String> {
    @Override
    public void initialize(Phone constraintAnnotation) {
        // Нічого не треба ініціалізувати
    }

    @Override
    public boolean isValid(String rawNumber, ConstraintValidatorContext context) {
        if (rawNumber == null) {
            return true; // null вважається валідним (NotNull треба ставити окремо)
        }
        try {
            Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse(rawNumber, "");
            return PhoneNumberUtil.getInstance().isValidNumber(number);
        } catch (NumberParseException e) {
            return false;
        }
    }
}
