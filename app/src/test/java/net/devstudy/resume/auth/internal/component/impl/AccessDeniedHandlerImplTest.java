package net.devstudy.resume.auth.internal.component.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

class AccessDeniedHandlerImplTest {

    @Test
    void redirectsToLoginWhenCsrfException() throws Exception {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/resume");
        request.setRequestURI("/resume/profile/edit");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new MissingCsrfTokenException("missing"));

        assertEquals("/resume/login?expired", response.getRedirectedUrl());
    }

    @Test
    void sendsForbiddenForNonCsrfAccessDenied() throws Exception {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals(403, response.getStatus());
    }

    @Test
    void redirectsToMeWhenAnonymousOnlyEndpointDenied() throws Exception {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/resume");
        request.setRequestURI("/resume/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals("/resume/me", response.getRedirectedUrl());
    }
}
