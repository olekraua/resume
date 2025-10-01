package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.devstudy.resume.annotation.constraints.FieldMatch;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;

public class FieldMatchConstraintValidator implements ConstraintValidator<FieldMatch, Object> {

    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(final FieldMatch constraintAnnotation) {
        firstFieldName = constraintAnnotation.first();
        secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true; // Nichts zu prüfen

        var wrapper = new BeanWrapperImpl(value);
        Object firstValue  = wrapper.getPropertyValue(firstFieldName);
        Object secondValue = wrapper.getPropertyValue(secondFieldName);

        boolean matches = Objects.equals(firstValue, secondValue);
        if (!matches) {
            // Standardverletzung ausschalten und an das zweite Feld hängen
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                   .addPropertyNode(secondFieldName)
                   .addConstraintViolation();
        }
        return matches;
    }

}
