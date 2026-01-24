package net.devstudy.resume.auth.internal.component.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

class AccessDeniedHandlerImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void redirectsToLoginWhenCsrfException() throws Exception {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl(OBJECT_MAPPER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/resume");
        request.setRequestURI("/resume/profile/edit");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new MissingCsrfTokenException("missing"));

        assertEquals("/resume/login?expired", response.getRedirectedUrl());
    }

    @Test
    void sendsForbiddenForNonCsrfAccessDenied() throws Exception {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl(OBJECT_MAPPER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals(403, response.getStatus());
    }

    @Test
    void redirectsToMeWhenAnonymousOnlyEndpointDenied() throws Exception {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl(OBJECT_MAPPER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/resume");
        request.setRequestURI("/resume/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals("/resume/me", response.getRedirectedUrl());
    }

    @Test
    void writesJsonErrorForApiRequests() throws Exception {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl(OBJECT_MAPPER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/private");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals(403, response.getStatus());
        assertNotNull(response.getContentType());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());

        JsonNode payload = OBJECT_MAPPER.readTree(response.getContentAsString());
        assertNotNull(payload.get("timestamp"));
        assertEquals(403, payload.get("status").asInt());
        assertEquals("Forbidden", payload.get("error").asText());
        assertEquals("Access denied", payload.get("message").asText());
        assertEquals("/api/private", payload.get("path").asText());
        assertFalse(payload.has("errors"));
    }
}
