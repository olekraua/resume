package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.devstudy.resume.annotation.constraints.FirstFieldLessThanSecond;
import org.springframework.beans.BeanWrapperImpl;

public class FirstFieldLessThanSecondConstraintValidator
        implements ConstraintValidator<FirstFieldLessThanSecond, Object> {

    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(FirstFieldLessThanSecond constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        var wrapper = new BeanWrapperImpl(value);
        Object first = wrapper.getPropertyValue(firstFieldName);
        Object second = wrapper.getPropertyValue(secondFieldName);

        // Regel nur prüfen, wenn beide gesetzt (typische Formular-Semantik)
        if (first == null || second == null)
            return true;

        if (!(first instanceof Comparable<?>))
            return false;

        @SuppressWarnings("unchecked")
        Comparable<Object> left = (Comparable<Object>) first;

        final int cmp;
        try {
            cmp = left.compareTo(second); // zweites Objekt NICHT casten
        } catch (ClassCastException ex) {
            return false; // Typen nicht vergleichbar (z. B. LocalDate vs String)
        }

        boolean ok = (cmp <= 0); // strict "first < second"
        if (!ok) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(secondFieldName)
                    .addConstraintViolation();
        }
        return ok;
    }
}
