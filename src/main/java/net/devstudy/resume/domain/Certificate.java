package net.devstudy.resume.domain;

import java.io.Serializable;
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
        final int prime = 31;
        int result = super.hashCode(); // entspricht Object#hashCode() (Identität)
        result = prime * result + (largeUrl == null ? 0 : largeUrl.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (smallUrl == null ? 0 : smallUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // effektives Verhalten der alten Methode war Identitätsgleichheit
        return this == obj;
    }
}

