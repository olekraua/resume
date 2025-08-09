package net.devstudy.resume.annotation.constraints;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMatch {
    String first() default "";
    String second() default "";
    String message() default "";
}
