package net.devstudy.resume.auth.api.model;

import java.io.Serial;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import net.devstudy.resume.shared.constants.Constants;
import net.devstudy.resume.profile.api.model.Profile;

public final class CurrentProfile extends User {
    @Serial
    private static final long serialVersionUID = 3850489832510630519L;

    private final Long id;
    private final String fullName;

    public CurrentProfile(Profile profile) {
        super(
            profile.getUid(),
            profile.getPassword(),
            true,  // enabled
            true,  // accountNonExpired
            true,  // credentialsNonExpired
            true,  // accountNonLocked
            List.of(new SimpleGrantedAuthority(Constants.UI.USER))
        );
        this.id = profile.getId();
        this.fullName = profile.getFullName();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return String.format("CurrentProfile [id=%s, username=%s]", id, getUsername());
    }
}


