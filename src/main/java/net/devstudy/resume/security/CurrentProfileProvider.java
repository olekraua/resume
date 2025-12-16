package net.devstudy.resume.security;

import net.devstudy.resume.model.CurrentProfile;

/**
 * Provides access to the current authenticated profile from the security context.
 */
public interface CurrentProfileProvider {
    CurrentProfile getCurrentProfile();

    Long getCurrentId();
}
