package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlSanitizedValidator implements ConstraintValidator<HtmlSanitized, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // стандартна поведінка Bean Validation

        // Повністю забороняємо HTML: жодних тегів/атрибутів/скриптів
        String cleaned = Jsoup.clean(value, Safelist.none());

        // Якщо очищене значення відрізняється — отже, в оригіналі був HTML → не валідно
        return cleaned.equals(value);
    }
}