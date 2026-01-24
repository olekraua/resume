package net.devstudy.resume.auth.internal.entity;

import java.io.Serial;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.shared.model.AbstractEntity;

@Entity
@Table(name = "profile_restore",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_profile_restore_profile", columnNames = "profile_id"),
                @UniqueConstraint(name = "uk_profile_restore_token", columnNames = "token")
        })
public class ProfileRestore extends AbstractEntity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant created;

    public ProfileRestore() {
    }

    public ProfileRestore(Profile profile, String token, Instant created) {
        this.profile = profile;
        this.token = token;
        this.created = created;
    }

    @Override
    public Long getId() {
        return id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}
