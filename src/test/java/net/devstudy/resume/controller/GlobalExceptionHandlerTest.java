package net.devstudy.resume.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

class GlobalExceptionHandlerTest {

    @Test
    void handleIllegalStateBuildsServerErrorViewWithRequestDetails() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test-illegal");

        ModelAndView mav = handler.handleIllegalState(new IllegalStateException("boom"), request);

        assertEquals("error/server-error", mav.getViewName());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, mav.getStatus());
        assertNotNull(mav.getModel());
        assertEquals("/test-illegal", mav.getModel().get("path"));
        assertEquals("GET", mav.getModel().get("method"));
        assertEquals("boom", mav.getModel().get("message"));
    }
}

