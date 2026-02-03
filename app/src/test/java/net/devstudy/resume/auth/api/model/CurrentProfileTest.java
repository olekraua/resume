package net.devstudy.resume.auth.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import net.devstudy.resume.shared.constants.Constants;
import net.devstudy.resume.profile.api.model.Profile;

class CurrentProfileTest {

    @Test
    void constructorCopiesProfileFields() {
        Profile profile = new Profile();
        profile.setId(7L);
        profile.setUid("john-doe");
        profile.setFirstName("John");
        profile.setLastName("Doe");

        CurrentProfile currentProfile = new CurrentProfile(profile);

        assertEquals(7L, currentProfile.getId());
        assertEquals("John Doe", currentProfile.getFullName());
        assertEquals("john-doe", currentProfile.getUsername());
        assertEquals("", currentProfile.getPassword());
        assertTrue(currentProfile.isEnabled());
        assertTrue(currentProfile.isAccountNonExpired());
        assertTrue(currentProfile.isAccountNonLocked());
        assertTrue(currentProfile.isCredentialsNonExpired());
        assertTrue(currentProfile.getAuthorities()
                .contains(new SimpleGrantedAuthority(Constants.UI.USER)));
    }

    @Test
    void toStringUsesIdAndUsername() {
        Profile profile = new Profile();
        profile.setId(3L);
        profile.setUid("user-3");
        profile.setFirstName("User");
        profile.setLastName("Three");

        CurrentProfile currentProfile = new CurrentProfile(profile);

        assertEquals("CurrentProfile [id=3, username=user-3]", currentProfile.toString());
    }
}
