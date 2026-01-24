package net.devstudy.resume.auth.internal.component.impl;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException, ServletException {
        LOGGER.info("Access denied: {}", ex.getMessage(), ex);
        if (ex instanceof CsrfException) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect(request.getContextPath() + "/login?expired");
            return;
        }
        if (isAnonymousOnlyPath(request)) {
            response.sendRedirect(request.getContextPath() + "/me");
            return;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private boolean isAnonymousOnlyPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if ("/login".equals(path) || "/register".equals(path) || "/restore".equals(path)) {
            return true;
        }
        return path.startsWith("/register/") || path.startsWith("/restore/");
    }
}
