package net.devstudy.resume.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Centralizes custom error handling for MVC controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ModelAndView handleNotFound(Exception ex, HttpServletRequest request) {
        return buildNotFoundView(request, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildServerErrorView(request, ex.getMessage());
    }

    private ModelAndView buildNotFoundView(HttpServletRequest request, String message) {
        ModelAndView mav = new ModelAndView("error/page-not-found");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("path", request.getRequestURI());
        mav.addObject("method", request.getMethod());
        mav.addObject("message", message);
        return mav;
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
