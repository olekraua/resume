package net.devstudy.resume.domain;

import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import net.devstudy.resume.annotation.constraints.EnglishLanguage;
import net.devstudy.resume.util.DataUtil;

public class Course extends AbstractFinishDateDocument<Long>
        implements Serializable, ProfileCollectionField, Comparable<Course> {
    private static final long serialVersionUID = 4206575925684228495L;

    @NotBlank
    @Size(max = 255)
    // дозволяємо літери/цифри/пробіли і базові знаки, щоб частково замінити
    // SafeHtml
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s'\\-.,()+/]*$")
    @EnglishLanguage(withSpecialSymbols = false)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s'\\-.,()+/]*$")
    @EnglishLanguage(withSpecialSymbols = false)
    private String school;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    @Override
    public int compareTo(Course o) {
        // як і було: спершу за датою завершення (null-и в кінець)
        return DataUtil.compareByFields(o.getFinishDate(), getFinishDate(), true);
    }

    // (не обов’язково, але більш надійно, ніж виклик super.equals/hashCode)
    @Override
    public int hashCode() {
        return Objects.hash(getFinishDate(), name, school);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Course other))
            return false;
        return Objects.equals(getFinishDate(), other.getFinishDate())
                && Objects.equals(name, other.name)
                && Objects.equals(school, other.school);
    }
}
