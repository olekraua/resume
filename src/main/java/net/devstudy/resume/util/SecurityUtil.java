package net.devstudy.resume.util;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.model.CurrentProfile;

public final class SecurityUtil {
    private SecurityUtil() {}
    public static CurrentProfile getCurrentProfile() { return null; } // kein Login -> null
    public static void authentificateWithRememberMe(Profile profile) { }
    public static void authentificate(Profile profile) { }
}
