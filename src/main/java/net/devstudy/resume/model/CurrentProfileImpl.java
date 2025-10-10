package net.devstudy.resume.model;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import net.devstudy.resume.Constants;
import net.devstudy.resume.domain.Profile;

public final class CurrentProfileImpl extends User implements CurrentProfile {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String fullName;

    public CurrentProfileImpl(@NonNull Profile profile) {
        super(
            Objects.requireNonNull(profile, "profile").getUid(),
            Objects.requireNonNull(profile.getPassword(), "profile.password"),
            true,  // enabled
            true,  // accountNonExpired
            true,  // credentialsNonExpired
            true,  // accountNonLocked
            List.<GrantedAuthority>of(new SimpleGrantedAuthority(Constants.USER)) // z.B. "ROLE_USER"
        );
        this.id = Objects.requireNonNull(profile.getId(), "profile.id");
        this.fullName = Objects.requireNonNull(profile.getFullName(), "profile.fullName");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUid() {
        return getUsername();
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return String.format("CurrentProfile [id=%s, username=%s]", id, getUsername());
    }
}

