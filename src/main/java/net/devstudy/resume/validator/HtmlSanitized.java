package net.devstudy.resume.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = HtmlSanitizedValidator.class)
public @interface HtmlSanitized {
    String message() default "HTML content is not allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}