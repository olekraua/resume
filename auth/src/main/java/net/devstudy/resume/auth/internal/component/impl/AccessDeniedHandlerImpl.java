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
        String path = resolvePath(request);
        boolean apiRequest = isApiPath(path);
        if (ex instanceof CsrfException) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            if (apiRequest) {
                writeJsonError(response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "csrf",
                        "CSRF token invalid or missing");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/login?expired");
            return;
        }
        if (isAnonymousOnlyPath(path)) {
            response.sendRedirect(request.getContextPath() + "/me");
            return;
        }
        if (apiRequest) {
            writeJsonError(response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "forbidden",
                    "Access denied");
            return;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private boolean isAnonymousOnlyPath(String path) {
        if ("/login".equals(path) || "/register".equals(path) || "/restore".equals(path)) {
            return true;
        }
        return path.startsWith("/register/") || path.startsWith("/restore/");
    }

    private boolean isApiPath(String path) {
        return path != null && path.startsWith("/api/");
    }

    private String resolvePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }
        return path;
    }

    private void writeJsonError(HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status
                + ",\"error\":\"" + error
                + "\",\"message\":\"" + message + "\"}");
    }
}
