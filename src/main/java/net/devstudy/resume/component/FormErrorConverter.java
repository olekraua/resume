package net.devstudy.resume.component;

import java.lang.annotation.Annotation;

import jakarta.annotation.Nonnull;

import org.springframework.validation.BindingResult;

/**
 * Converter that transforms validation annotations into Spring field errors.
 * Works with Java 21 + Spring Boot 3
 */
public interface FormErrorConverter {

    void convertToFieldError(
        @Nonnull Class<? extends Annotation> validationAnnotationClass,
        @Nonnull Object formInstance,
        @Nonnull BindingResult bindingResult
    );
}
