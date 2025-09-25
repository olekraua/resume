package net.devstudy.resume.domain;

import java.io.Serializable;
import java.util.Objects;
import jakarta.validation.constraints.Pattern;
import static net.devstudy.resume.util.SanitizationUtils.cleanToPlainText;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Size;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;
import net.devstudy.resume.validator.HtmlSanitized;

/**
 * Certificate model.
 * Verhalten unverändert:
 * - equals: Identitätsvergleich (wie zuvor effektiv umgesetzt).
 * - hashCode: basiert auf super.hashCode() und den drei String-Feldern.
 */
public class Certificate implements Serializable, ProfileCollectionField {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private String largeUrl;

    @JsonIgnore
    private String smallUrl;

    @EnglishLanguage
    @Size(max = 160)
    @HtmlSanitized
    private String name;

    @EnglishLanguage
    @Size(max = 120)
    @HtmlSanitized
    // Додатково (необов’язково): заборона символів < і >
    @Pattern(regexp = "^[^<>]*$", message = "HTML angle brackets are not allowed")
    private String issuer;

    public Certificate() {
        // no-args constructor
    }

    public String getLargeUrl() {
        return largeUrl;
    }

    public void setLargeUrl(String largeUrl) {
        this.largeUrl = largeUrl;
    }

    public String getSmallUrl() {
        return smallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        this.smallUrl = smallUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = cleanToPlainText(name);
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = cleanToPlainText(issuer);
    }

    @Override
    public int hashCode() {
    return Objects.hash(super.hashCode(), largeUrl, name, smallUrl, issuer);
}


    @Override
    public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Certificate other)) return false;
    return Objects.equals(largeUrl, other.largeUrl)
        && Objects.equals(name, other.name)
        && Objects.equals(smallUrl, other.smallUrl)
        && Objects.equals(issuer, other.issuer);
}

}

