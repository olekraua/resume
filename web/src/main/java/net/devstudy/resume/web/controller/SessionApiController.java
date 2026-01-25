package net.devstudy.resume.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.auth.api.model.CurrentProfile;
import net.devstudy.resume.auth.api.security.CurrentProfileProvider;

@RestController
@RequiredArgsConstructor
public class SessionApiController {

    private final CurrentProfileProvider currentProfileProvider;

    @GetMapping("/api/me")
    public SessionResponse session() {
        CurrentProfile currentProfile = currentProfileProvider.getCurrentProfile();
        if (currentProfile == null) {
            return new SessionResponse(false, null, null);
        }
        return new SessionResponse(true, currentProfile.getUsername(), currentProfile.getFullName());
    }

    public record SessionResponse(
            boolean authenticated,
            String uid,
            String fullName
    ) {
    }
}
