package net.devstudy.resume.domain;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Pattern;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;
import net.devstudy.resume.model.LanguageLevel;
import net.devstudy.resume.model.LanguageType;

/**
 * Модель мови для профілю користувача.
 */
public class Language implements Serializable, ProfileCollectionField, Comparable<Language> {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private LanguageLevel level;

    @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "Only letters, spaces, apostrophes and dashes allowed")
    @EnglishLanguage(withSpecialSymbols = false, withNumbers = false, withPunctuations = false)
    private String name;

    private LanguageType type;

    public Language() {
        // Default constructor required by frameworks (Jackson, JPA, Spring)
    }

    public LanguageLevel getLevel() {
        return level;
    }

    public void setLevel(LanguageLevel level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LanguageType getType() {
        return type;
    }

    public void setType(LanguageType type) {
        this.type = type;
    }

    public boolean isHasLanguageType() {
        return type != LanguageType.ALL;
    }

    @Override
    public String toString() {
        return "Language[level=%s, name=%s, type=%s]".formatted(level, name, type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Language other)) return false;
        return Objects.equals(level, other.level)
                && Objects.equals(name, other.name)
                && type == other.type;
    }

    @Override
    public int compareTo(Language o) {
        return Objects.compare(this.name, o.name, String.CASE_INSENSITIVE_ORDER);
    }
}

