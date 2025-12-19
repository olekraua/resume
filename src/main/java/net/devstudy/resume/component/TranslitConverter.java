package net.devstudy.resume.component;

import org.springframework.lang.NonNull;

public interface TranslitConverter {

    @NonNull
    String translit(@NonNull String text);
}
