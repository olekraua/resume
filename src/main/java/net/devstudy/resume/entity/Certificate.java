package net.devstudy.resume.entity;

import java.io.Serial;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * @author devstudy
 * @see http://devstudy.net
 */
@Entity
@Table(name = "certificate")
public class Certificate extends AbstractEntity<Long> implements ProfileEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "CERTIFICATE_ID_GENERATOR", sequenceName = "CERTIFICATE_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CERTIFICATE_ID_GENERATOR")
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(name = "large_url", nullable = false, length = 255)
    private String largeUrl;

    @Column(name = "small_url", nullable = false, length = 255)
    private String smallUrl;

    @Column(nullable = false, length = 50)
    private String name;

    // bi-directional many-to-one association to Profile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profile", nullable = false)
    @JsonIgnore
    private Profile profile;

    public Certificate() {
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Override
    public int hashCode() {
        // Beibehaltung der ursprünglichen Semantik (id, largeUrl, name, smallUrl +
        // super)
        return Objects.hash(super.hashCode(), id, largeUrl, name, smallUrl);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Certificate other))
            return false;
        return Objects.equals(id, other.id)
                && Objects.equals(largeUrl, other.largeUrl)
                && Objects.equals(name, other.name)
                && Objects.equals(smallUrl, other.smallUrl);
    }
}
