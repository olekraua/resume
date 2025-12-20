package net.devstudy.resume.component;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface TemplateResolver {

    @NonNull
    String resolve(@NonNull String template, @Nullable Object model);
}
