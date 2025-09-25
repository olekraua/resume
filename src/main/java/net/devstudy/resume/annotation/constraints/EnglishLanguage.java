package net.devstudy.resume.annotation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import net.devstudy.resume.validator.EnglishLanguageConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that a field contains English language characters,
 * optionally allowing numbers, punctuation and special symbols.
 *
 * @author devstudy
 * @see http://devstudy.net
 */
@Documented
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = EnglishLanguageConstraintValidator.class)
public @interface EnglishLanguage {

    /**
     * Default validation message.
     */
    String message() default "EnglishLanguage";

    /**
     * Allow digits 0–9.
     */
    boolean withNumbers() default true;

    /**
     * Allow punctuation symbols (e.g., . , ? ! - : ...).
     */
    boolean withPunctuations() default true;

    /**
     * Allow special symbols (e.g., ~ # $ % ^ & * ...).
     */
    boolean withSpecialSymbols() default true;

    /**
     * Validation groups.
     */
    Class<?>[] groups() default { };

    /**
     * Payload for clients of the Bean Validation API.
     */
    Class<? extends Payload>[] payload() default { };
}
