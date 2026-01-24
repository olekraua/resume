package net.devstudy.resume.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.auth.api.dto.RestoreAccessForm;
import net.devstudy.resume.auth.api.dto.RestorePasswordForm;
import net.devstudy.resume.auth.api.service.RestoreAccessService;

class RestoreAccessControllerTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void restoreFormAddsFormWhenMissing() {
        RestoreAccessController controller = new RestoreAccessController(mock(RestoreAccessService.class), false);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.restoreForm(model);

        assertEquals("auth/restore", view);
        assertTrue(model.containsAttribute("restoreAccessForm"));
    }

    @Test
    void restoreFormKeepsExistingForm() {
        RestoreAccessController controller = new RestoreAccessController(mock(RestoreAccessService.class), false);
        ExtendedModelMap model = new ExtendedModelMap();
        RestoreAccessForm existing = new RestoreAccessForm();
        model.addAttribute("restoreAccessForm", existing);

        String view = controller.restoreForm(model);

        assertEquals("auth/restore", view);
        assertSame(existing, model.get("restoreAccessForm"));
    }

    @Test
    void restoreReturnsFormWhenBindingErrors() {
        RestoreAccessService service = mock(RestoreAccessService.class);
        RestoreAccessController controller = new RestoreAccessController(service, false);
        RestoreAccessForm form = new RestoreAccessForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "restoreAccessForm");
        bindingResult.reject("error");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.restore(form, bindingResult, redirectAttributes, new ExtendedModelMap());

        assertEquals("auth/restore", view);
        verifyNoInteractions(service);
    }

    @Test
    void restoreAddsLinkAndRedirectsOnSuccess() {
        RestoreAccessService service = mock(RestoreAccessService.class);
        RestoreAccessController controller = new RestoreAccessController(service, false);
        RestoreAccessForm form = new RestoreAccessForm();
        form.setIdentifier("user");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "restoreAccessForm");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        setRequestContext("http", "example.com", 8080, "/app");

        String appHost = "http://example.com:8080/app";
        when(service.requestRestore("user", appHost)).thenReturn("link");

        String view = controller.restore(form, bindingResult, redirectAttributes, new ExtendedModelMap());

        assertEquals("redirect:/restore/success", view);
        assertEquals("link", redirectAttributes.getFlashAttributes().get("restoreLink"));
        verify(service).requestRestore("user", appHost);
    }

    @Test
    void restoreRedirectsWhenServiceThrows() {
        RestoreAccessService service = mock(RestoreAccessService.class);
        RestoreAccessController controller = new RestoreAccessController(service, false);
        RestoreAccessForm form = new RestoreAccessForm();
        form.setIdentifier("user");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "restoreAccessForm");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        setRequestContext("http", "localhost", 8080, "");

        String appHost = "http://localhost:8080";
        doThrow(new IllegalArgumentException("bad")).when(service).requestRestore("user", appHost);

        String view = controller.restore(form, bindingResult, redirectAttributes, new ExtendedModelMap());

        assertEquals("redirect:/restore/success", view);
        assertTrue(redirectAttributes.getFlashAttributes().isEmpty());
        verify(service).requestRestore("user", appHost);
    }

    @Test
    void restoreSuccessSetsShowRestoreLink() {
        RestoreAccessController controller = new RestoreAccessController(mock(RestoreAccessService.class), true);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.restoreSuccess(model);

        assertEquals("auth/restore-success", view);
        assertEquals(Boolean.TRUE, model.get("showRestoreLink"));
    }

    @Test
    void restorePasswordFormRedirectsWhenTokenMissing() {
        RestoreAccessService service = mock(RestoreAccessService.class);
        RestoreAccessController controller = new RestoreAccessController(service, false);
        when(service.findProfileByToken("token")).thenReturn(Optional.empty());

        String view = controller.restorePasswordForm("token", new ExtendedModelMap());

        assertEquals("redirect:/restore?invalid", view);
    }

    @Test
    void restorePasswordFormRendersWhenTokenValid() {
        RestoreAccessService service = mock(RestoreAccessService.class);
        RestoreAccessController controller = new RestoreAccessController(service, false);
        when(service.findProfileByToken("token")).thenReturn(Optional.of(new Profile()));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.restorePasswordForm("token", model);

        assertEquals("auth/restore-password", view);
        assertEquals("token", model.get("token"));
        assertTrue(model.get("restorePasswordForm") instanceof RestorePasswordForm);
    }

    @Test
    void restorePasswordReturnsFormWhenBindingErrors() {
        RestoreAccessService service = mock(RestoreAccessService.class);
        RestoreAccessController controller = new RestoreAccessController(service, false);
        RestorePasswordForm form = new RestorePasswordForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "restorePasswordForm");
        bindingResult.reject("error");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.restorePassword("token", form, bindingResult, model);

        assertEquals("auth/restore-password", view);
        assertEquals("token", model.get("token"));
        verify(service, never()).resetPassword(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void restorePasswordRedirectsOnSuccess() {
        RestoreAccessService service = mock(RestoreAccessService.class);
        RestoreAccessController controller = new RestoreAccessController(service, false);
        RestorePasswordForm form = new RestorePasswordForm();
        form.setPassword("new-pass");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "restorePasswordForm");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.restorePassword("token", form, bindingResult, model);

        assertEquals("redirect:/login?restored", view);
        verify(service).resetPassword("token", "new-pass");
    }

    @Test
    void restorePasswordRedirectsWhenTokenInvalid() {
        RestoreAccessService service = mock(RestoreAccessService.class);
        RestoreAccessController controller = new RestoreAccessController(service, false);
        RestorePasswordForm form = new RestorePasswordForm();
        form.setPassword("new-pass");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "restorePasswordForm");
        ExtendedModelMap model = new ExtendedModelMap();
        doThrow(new IllegalArgumentException("bad")).when(service).resetPassword("token", "new-pass");

        String view = controller.restorePassword("token", form, bindingResult, model);

        assertEquals("redirect:/restore?invalid", view);
        verify(service).resetPassword("token", "new-pass");
    }

    private void setRequestContext(String scheme, String host, int port, String contextPath) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme(scheme);
        request.setServerName(host);
        request.setServerPort(port);
        request.setContextPath(contextPath);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
