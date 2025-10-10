package net.devstudy.resume.util;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.model.CurrentProfileImpl;
import net.devstudy.resume.service.impl.RememberMeService;

/**
 * Security helpers for programmatic auth / logout.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static @Nullable CurrentProfile getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return (principal instanceof CurrentProfile cp) ? cp : null;
    }

    /** Programmatically authenticate a Profile (e.g., right after registration). */
    public static Authentication authenticate(Profile profile) {
        CurrentProfileImpl current = new CurrentProfileImpl(profile);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                current,
                current.getPassword(),
                current.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        return auth;
    }

    /**
     * Authenticate and issue remember-me cookie using your custom
     * RememberMeService.
     */
    public static void authentificateWithRememberMe(Profile profile) {
    // 1) Authentifizieren (bevorzugt die Variante mit createEmptyContext())
    Authentication authentication = authenticate(profile);

    // 2) Request/Response sicher holen
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
        throw new IllegalStateException("No ServletRequestAttributes bound to current thread.");
    }
    jakarta.servlet.http.HttpServletRequest request = attrs.getRequest();
    jakarta.servlet.http.HttpServletResponse response = attrs.getResponse();
    if (response == null) {
        throw new IllegalStateException("No HttpServletResponse bound to current thread.");
    }

    // 3) ApplicationContext holen
    org.springframework.web.context.WebApplicationContext ctx =
        (org.springframework.web.context.WebApplicationContext)
            request.getServletContext().getAttribute(
                org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

    // 4) Remember-me Token setzen
    net.devstudy.resume.service.impl.RememberMeService rememberMeService = ctx.getBean(RememberMeService.class);
    rememberMeService.createAutoLoginToken(request, response, authentication);
}


    public static boolean isCurrentProfileAuthenticated() {
        return getCurrentProfile() != null;
    }

    public static String generateNewRestoreAccessToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Log out the current user in a Spring-Security-6-friendly way:
     * - clears SecurityContext
     * - optionally lets your RememberMeService clear its cookie if it implements
     * LogoutHandler
     */
    public static void logout() {
    ServletRequestAttributes attrs = getRequestAttributesOrThrow();
    HttpServletRequest request = attrs.getRequest();
    HttpServletResponse response = attrs.getResponse();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (response == null) {
        throw new IllegalStateException("No HttpServletResponse bound to current thread.");
    }

    // Optional: Remember-Me-Logout, falls dein RememberMeService auch LogoutHandler ist
    LogoutHandler rememberMeLogout = resolveRememberMeLogoutHandler(request);
    if (rememberMeLogout != null) {
        rememberMeLogout.logout(request, response, auth);
    }

    // Immer: SecurityContext + Session invalidieren
    new SecurityContextLogoutHandler().logout(request, response, auth);
}

    /* ---------- helpers ---------- */

    private static @Nullable LogoutHandler resolveRememberMeLogoutHandler(HttpServletRequest request) {
    var ctxAttr = request.getServletContext()
            .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    if (!(ctxAttr instanceof WebApplicationContext ctx)) return null;

    // Try by name first
    if (ctx.containsBean("rememberMeService")) {
        var bean = ctx.getBean("rememberMeService");
        if (bean instanceof LogoutHandler lh) return lh;
    }

    // Try by type second
    try {
        var svc = ctx.getBean(RememberMeService.class);
        if (svc instanceof LogoutHandler lh) return lh;
    } catch (Exception ignore) {
        // no bean found, ignore
    }

    return null;
}

    private static ServletRequestAttributes getRequestAttributesOrThrow() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            throw new IllegalStateException("No ServletRequestAttributes bound to current thread.");
        }
        return sra;
    }
}
