package net.devstudy.resume.annotation.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Композитна перевірка складності пароля
 * (довжина, не-null, мін. кількість цифр/верхніх/нижніх/спецсимволів).
 *
 * Працює з Java 21 + Spring Boot 3 (Jakarta Validation).
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { }) // валідатор не потрібен, бо це композиція
@Size(min = 8)
@NotNull
@MinDigitCount( value = 1 )
@MinUpperCharCount( value = 1 )
@MinLowerCharCount( value = 1 )
@MinSpecCharCount( value = 1 )
// Забажай — показувати одне повідомлення зверху замість багатьох від підлеглих
@ReportAsSingleViolation
public @interface PasswordStrength {

    String message() default "PasswordStrength";

    Class<? extends Payload>[] payload() default { };

    Class<?>[] groups() default { };
}
