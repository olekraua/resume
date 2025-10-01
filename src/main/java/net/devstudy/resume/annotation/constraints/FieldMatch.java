package net.devstudy.resume.annotation.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import net.devstudy.resume.validator.FieldMatchConstraintValidator;

/**
 * Validation annotation to validate that 2 fields have the same value. An array
 * of fields and their matching confirmation fields can be supplied.
 *
 * Example, compare 1 pair of fields:
 * 
 * @FieldMatch(first = "password", second = "confirmPassword", message ="The password fields must match")
 *
 * Example, compare more than 1 pair of fields: 
 * @FieldMatch.List({ @FieldMatch(first = "password", second = "confirmPassword", 
 * message = "The password fields must match"),
 * @FieldMatch(first = "email", second = "confirmEmail", message ="The email fields must match")})
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = FieldMatchConstraintValidator.class)
@Documented
@Repeatable(FieldMatch.List.class)
public @interface FieldMatch {
    String message() default "FieldMatch";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String first();
    String second();

    /**
	 * Defines several <code>@FieldMatch</code> annotations on the same element
	 *
	 * @see FieldMatch
	 */
    @Target({ TYPE, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        FieldMatch[] value();
    }
}
