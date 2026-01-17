package net.devstudy.resume.component.impl;

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
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new MissingCsrfTokenException("missing"));

        assertEquals("/resume/login?expired", response.getRedirectedUrl());
    }

    @Test
    void sendsForbiddenForNonCsrfAccessDenied() throws Exception {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals(403, response.getStatus());
    }
}
