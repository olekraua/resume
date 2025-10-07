package net.devstudy.resume.domain;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Pattern;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;

public class Skill implements Serializable, ProfileCollectionField, Comparable<Skill> {
    private static final long serialVersionUID = 1L;

    private Short idCategory;

    @JsonIgnore
    // SafeHtml видалено в Hibernate Validator 7. За потреби можна обмежити свій
    // набір символів патерном:
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s'\\-.,()+/]*$", message = "Недопустимі символи y назві категорії")
    @EnglishLanguage(withSpecialSymbols = false, withNumbers = false)
    private String category;

    // SafeHtml теж прибрано; можна залишити тільки власну анотацію
    @EnglishLanguage
    private String value;

    public Skill() {
        // no-arg ctor потрібен для Jackson/Spring Data
    }

    public Short getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Short idCategory) {
        this.idCategory = idCategory;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCategory, category, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Skill other))
            return false;
        return Objects.equals(idCategory, other.idCategory)
                && Objects.equals(category, other.category)
                && Objects.equals(value, other.value);
    }

    @Override
    public int compareTo(Skill o) {
        int cmp = nullSafeCompare(this.idCategory, o.idCategory);
        if (cmp != 0)
            return cmp;
        cmp = nullSafeCompareIgnoreCase(this.category, o.category);
        if (cmp != 0)
            return cmp;
        return nullSafeCompareIgnoreCase(this.value, o.value);
    }

    private static <T extends Comparable<T>> int nullSafeCompare(T a, T b) {
        if (a == b)
            return 0;
        if (a == null)
            return 1; // null-и в кінець
        if (b == null)
            return -1;
        return a.compareTo(b);
    }

    private static int nullSafeCompareIgnoreCase(String a, String b) {
        if (Objects.equals(a, b))
            return 0;
        if (a == null)
            return 1;
        if (b == null)
            return -1;
        return a.compareToIgnoreCase(b);
    }
}
