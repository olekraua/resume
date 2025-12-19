package net.devstudy.resume.service;

import java.util.Optional;

import net.devstudy.resume.entity.Profile;

public interface RestoreAccessService {

    String requestRestore(String identifier, String appHost);

    Optional<Profile> findProfileByToken(String token);

    void resetPassword(String token, String rawPassword);
}
