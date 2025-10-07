package net.devstudy.resume.domain;

import java.io.Serializable;

import org.hibernate.validator.constraints.URL; // OK у HV 7 (jakarta-enabled)

import jakarta.validation.constraints.Pattern;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;

public class Contacts implements Serializable {
    private static final long serialVersionUID = -3685720846934765841L;

    // Дозволяємо літери, цифри, крапки, дефіси, підкреслення, плюс — від 3 символів
    @Pattern(regexp = "^[\\p{L}\\p{N}._+\\-]{3,}$", message = "Неприпустимий формат Skype")
    @EnglishLanguage
    private String skype;

    @EnglishLanguage
    @URL(host = "vk.com")
    private String vkontakte;

    @EnglishLanguage
    @URL(host = "facebook.com")
    private String facebook;

    @EnglishLanguage
    @URL(host = "linkedin.com")
    private String linkedin;

    @EnglishLanguage
    @URL(host = "github.com")
    private String github;

    @EnglishLanguage
    @URL(host = "stackoverflow.com")
    private String stackoverflow;

    public Contacts() {
        // no-args constructor
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getStackoverflow() {
        return stackoverflow;
    }

    public void setStackoverflow(String stackoverflow) {
        this.stackoverflow = stackoverflow;
    }
}
