package net.devstudy.resume.component;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class FormErrorConverter {
    public void convertToFieldError(Class<?> annotation, Object target, BindingResult bindingResult) {
        // TODO: echte Umsetzung später
    }
}
