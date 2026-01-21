package net.devstudy.resume.auth.security;

import net.devstudy.resume.auth.model.CurrentProfile;

/**
 * Provides access to the current authenticated profile from the security context.
 */
public interface CurrentProfileProvider {
    CurrentProfile getCurrentProfile();

    Long getCurrentId();
}
