package net.devstudy.resume.form;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import net.devstudy.resume.annotation.constraints.EnglishLanguage;
import net.devstudy.resume.domain.Profile;

/**
 * Info-Formular (Java 21, Spring Boot 3)
 */
public class InfoForm {

    @EnglishLanguage // ← Stelle sicher, dass dein eigenes Constraint auf jakarta.* migriert ist
    @Size(max = 4000, message = "Info darf höchstens {max} Zeichen haben")
    @Pattern(regexp = "^[^<>]*$", message = "HTML-Tags sind nicht erlaubt")
    private String info;

    public InfoForm() {}

    public InfoForm(String info) {
        this.info = trim(info);
    }

    public InfoForm(Profile profile) {
        this.info = profile != null ? trim(profile.getInfo()) : null;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = trim(info);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}

