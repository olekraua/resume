package net.devstudy.resume.auth.service.impl;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.auth.model.CurrentProfile;
import net.devstudy.resume.profile.repository.storage.ProfileRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentProfileDetailsService implements UserDetailsService {

    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Profile profile = profileRepository.findByUid(username)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found: " + username));
        return new CurrentProfile(profile);
    }
}
