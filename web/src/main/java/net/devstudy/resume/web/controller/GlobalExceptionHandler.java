package net.devstudy.resume.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Centralizes custom error handling for MVC controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildServerErrorView(request, ex.getMessage());
    }

    private ModelAndView buildServerErrorView(HttpServletRequest request, String message) {
        ModelAndView mav = new ModelAndView("error/server-error");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("path", request.getRequestURI());
        mav.addObject("method", request.getMethod());
        mav.addObject("message", message);
        return mav;
    }
}
