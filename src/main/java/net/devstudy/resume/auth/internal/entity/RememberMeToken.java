package net.devstudy.resume.auth.internal.entity;

import java.io.Serial;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.shared.model.AbstractEntity;

@Entity
@Table(name = "remember_me_token",
        indexes = {
                @Index(name = "idx_remember_me_profile", columnList = "profile_id")
        })
public class RememberMeToken extends AbstractEntity<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 64, nullable = false)
    private String series;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(name = "last_used", nullable = false)
    private Instant lastUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Profile profile;

    public RememberMeToken() {
    }

    public RememberMeToken(String series, String token, Instant lastUsed, Profile profile) {
        this.series = series;
        this.token = token;
        this.lastUsed = lastUsed;
        this.profile = profile;
    }

    @Override
    public String getId() {
        return series;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Instant lastUsed) {
        this.lastUsed = lastUsed;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
