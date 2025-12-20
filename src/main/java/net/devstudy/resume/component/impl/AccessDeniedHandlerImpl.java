package net.devstudy.resume.component.impl;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);

    private final Environment environment;

    public AccessDeniedHandlerImpl(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException, ServletException {
        LOGGER.info("Access denied: {}", ex.getMessage(), ex);
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            response.sendRedirect("/error?status=403");
        } else {
            throw new ServletException(ex);
        }
    }
}
