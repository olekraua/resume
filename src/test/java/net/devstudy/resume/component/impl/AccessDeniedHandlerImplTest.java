package net.devstudy.resume.component.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

class AccessDeniedHandlerImplTest {

    @Test
    void redirectsToErrorWhenProdProfileActive() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl(environment);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals("/error?status=403", response.getRedirectedUrl());
    }

    @Test
    void throwsServletExceptionOutsideProd() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl(environment);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ServletException ex = assertThrows(ServletException.class,
                () -> handler.handle(request, response, new AccessDeniedException("denied")));

        assertTrue(ex.getCause() instanceof AccessDeniedException);
    }
}
