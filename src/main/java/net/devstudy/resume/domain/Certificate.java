package net.devstudy.resume.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.devstudy.resume.annotation.constraints.EnglishLanguage;

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
    private String name;

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
        this.name = name;
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

