package net.devstudy.resume.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.devstudy.resume.annotation.constraints.Phone;

public class PhoneConstraintValidator implements ConstraintValidator<Phone, CharSequence> {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String rawNumber = value.toString().trim();
        if (rawNumber.isEmpty()) {
            return true;
        }
        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(rawNumber, "ZZ");
            return phoneNumberUtil.isValidNumber(number);
        } catch (NumberParseException ex) {
            return false;
        }
    }
}
