package net.devstudy.resume.web.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.auth.api.model.CurrentProfile;
import net.devstudy.resume.auth.api.security.CurrentProfileProvider;

@Controller
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ui.mvc.enabled", havingValue = "false", matchIfMissing = true)
public class LegacyRouteRedirectController {

    private final CurrentProfileProvider currentProfileProvider;

    @GetMapping({"/app", "/app/"})
    public String redirectLegacyAppRoot(HttpServletRequest request) {
        return buildRedirect("/", request);
    }

    @GetMapping("/app/{*path}")
    public String redirectLegacyApp(@PathVariable String path, HttpServletRequest request) {
        String target = path == null || path.isBlank() ? "/" : "/" + path;
        return buildRedirect(target, request);
    }

    @GetMapping({"/{uid}/edit", "/{uid}/edit/"})
    public String redirectLegacyEditRoot(@PathVariable String uid, HttpServletRequest request) {
        return buildRedirect("/edit", request);
    }

    @GetMapping("/{uid}/edit/{*path}")
    public String redirectLegacyEdit(@PathVariable String uid, @PathVariable String path,
            HttpServletRequest request) {
        String target = path == null || path.isBlank() ? "/edit" : "/edit/" + path;
        return buildRedirect(target, request);
    }

    @GetMapping({"/me", "/me/"})
    public String redirectMe(HttpServletRequest request) {
        CurrentProfile currentProfile = currentProfileProvider.getCurrentProfile();
        if (currentProfile == null) {
            return "redirect:/login";
        }
        return buildRedirect("/" + currentProfile.getUsername(), request);
    }

    private String buildRedirect(String path, HttpServletRequest request) {
        String normalized = (path == null || path.isBlank()) ? "/" : path;
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return "redirect:" + normalized;
        }
        return "redirect:" + normalized + "?" + query;
    }
}
