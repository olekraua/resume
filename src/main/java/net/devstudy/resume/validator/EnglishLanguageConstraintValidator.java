package net.devstudy.resume.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import net.devstudy.resume.annotation.constraints.EnglishLanguage;

/**
 * Validator für @EnglishLanguage
 * - kompatibel mit Spring Boot 3 (Jakarta Validation)
 * - gleiche Semantik wie zuvor (null ist gültig; nur erlaubte Zeichen)
 */
public class EnglishLanguageConstraintValidator implements ConstraintValidator<EnglishLanguage, String> {

    private boolean withNumbers;
    private boolean withPunctuations;
    private boolean withSpechSymbols; // Name bleibt bewusst identisch zur Annotation

    @Override
    public void initialize(EnglishLanguage constraintAnnotation) {
        this.withNumbers      = constraintAnnotation.withNumbers();
        this.withPunctuations = constraintAnnotation.withPunctuations();
        this.withSpechSymbols = constraintAnnotation.withSpecialSymbols();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // unverändertes Verhalten
        }

        final String allowed = getValidationTemplate();

        // kleine Optimierung ggü. String#indexOf: wir bauen eine Lookup-Tabelle
        int max = 0;
        for (int i = 0; i < allowed.length(); i++) {
            max = Math.max(max, allowed.charAt(i));
        }
        final boolean[] mask = new boolean[max + 1];
        for (int i = 0; i < allowed.length(); i++) {
            mask[allowed.charAt(i)] = true;
        }

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch > max || !mask[ch]) {
                return false;
            }
        }
        return true;
    }

    // Konstanten bleiben unverändert (inkl. der Bezeichner)
    private static final String SPETCH_SYMBOLS = "~#$%^&*-+=_\\|/@`!'\";:><,.?{}";
    private static final String PUNCTUATIONS  = ".,?!-:()'\"[]{}; \t\n";
    private static final String NUMBERS       = "0123456789";
    private static final String LETTERS       = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private String getValidationTemplate() {
        StringBuilder template = new StringBuilder(LETTERS);
        if (withNumbers) {
            template.append(NUMBERS);
        }
        if (withPunctuations) {
            template.append(PUNCTUATIONS);
        }
        if (withSpechSymbols) {
            template.append(SPETCH_SYMBOLS);
        }
        return template.toString();
    }
}
