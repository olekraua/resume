package net.devstudy.resume.controller;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import net.devstudy.resume.config.UiProperties;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.exception.UidAlreadyExistsException;
import net.devstudy.resume.form.ChangePasswordForm;
import net.devstudy.resume.security.CurrentProfileProvider;
import net.devstudy.resume.service.ProfileService;
import net.devstudy.resume.service.UidSuggestionService;

@WebMvcTest(controllers = AccountController.class)
@Import(UiProperties.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CurrentProfileProvider currentProfileProvider;

    @MockitoBean
    private UidSuggestionService uidSuggestionService;

    @Test
    @WithMockUser
    void rendersChangePasswordForm() throws Exception {
        mockMvc.perform(get("/account/password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-password"))
                .andExpect(model().attributeExists("changePasswordForm"))
                .andExpect(model().attribute("changePasswordForm", instanceOf(ChangePasswordForm.class)));
    }

    @Test
    @WithMockUser
    void returnsFormWhenBindingErrors() throws Exception {
        mockMvc.perform(post("/account/password")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-password"));
    }

    @Test
    @WithMockUser
    void redirectsToLoginWhenCurrentUserMissing() throws Exception {
        when(currentProfileProvider.getCurrentId()).thenReturn(null);

        mockMvc.perform(post("/account/password")
                        .with(csrf())
                        .param("currentPassword", "current")
                        .param("newPassword", "newpass")
                        .param("confirmPassword", "newpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void returnsFormWhenCurrentPasswordInvalid() throws Exception {
        Profile profile = new Profile();
        profile.setPassword("encoded");
        when(currentProfileProvider.getCurrentId()).thenReturn(1L);
        when(profileService.findById(1L)).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("current", "encoded")).thenReturn(false);

        mockMvc.perform(post("/account/password")
                        .with(csrf())
                        .param("currentPassword", "current")
                        .param("newPassword", "newpass")
                        .param("confirmPassword", "newpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-password"))
                .andExpect(model().attribute("errorMessage", "Невірний поточний пароль"));

        verify(profileService, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    @WithMockUser
    void redirectsOnSuccessfulPasswordChange() throws Exception {
        Profile profile = new Profile();
        profile.setPassword("encoded");
        when(currentProfileProvider.getCurrentId()).thenReturn(1L);
        when(profileService.findById(1L)).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("current", "encoded")).thenReturn(true);

        mockMvc.perform(post("/account/password")
                        .with(csrf())
                        .param("currentPassword", "current")
                        .param("newPassword", "newpass")
                        .param("confirmPassword", "newpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/account/password?success"));

        verify(profileService).updatePassword(1L, "newpass");
    }

    @Test
    @WithMockUser
    void redirectsToLoginWhenCurrentUserMissingForLoginForm() throws Exception {
        when(currentProfileProvider.getCurrentId()).thenReturn(null);

        mockMvc.perform(get("/account/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void redirectsToLoginWhenProfileMissingForLoginForm() throws Exception {
        when(currentProfileProvider.getCurrentId()).thenReturn(1L);
        when(profileService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/account/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void rendersChangeLoginForm() throws Exception {
        Profile profile = new Profile();
        profile.setUid("owner-user");
        when(currentProfileProvider.getCurrentId()).thenReturn(1L);
        when(profileService.findById(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/account/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-login"))
                .andExpect(model().attribute("currentUid", "owner-user"))
                .andExpect(model().attribute("changeLoginForm", hasProperty("newUid", is("owner-user"))));
    }

    @Test
    @WithMockUser
    void redirectsToLoginWhenCurrentUserMissingForChangeLogin() throws Exception {
        when(currentProfileProvider.getCurrentId()).thenReturn(null);

        mockMvc.perform(post("/account/login")
                        .with(csrf())
                        .param("newUid", "new-user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void returnsFormWhenBindingErrorsOnChangeLogin() throws Exception {
        Profile profile = new Profile();
        profile.setUid("current-user");
        when(currentProfileProvider.getCurrentId()).thenReturn(1L);
        when(profileService.findById(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(post("/account/login")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-login"))
                .andExpect(model().attribute("currentUid", "current-user"));
    }

    @Test
    @WithMockUser
    void returnsFormWhenUidAlreadyExists() throws Exception {
        Profile profile = new Profile();
        profile.setUid("current-user");
        when(currentProfileProvider.getCurrentId()).thenReturn(1L);
        when(profileService.findById(1L)).thenReturn(Optional.of(profile));
        when(uidSuggestionService.suggest("taken-uid")).thenReturn(List.of("taken-uid1"));
        doThrow(new UidAlreadyExistsException("taken-uid"))
                .when(profileService)
                .updateUid(1L, "taken-uid");

        mockMvc.perform(post("/account/login")
                        .with(csrf())
                        .param("newUid", "taken-uid"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-login"))
                .andExpect(model().attribute("currentUid", "current-user"))
                .andExpect(model().attribute("uidSuggestions", List.of("taken-uid1")))
                .andExpect(model().attribute("uidError", "Uid already exists: taken-uid"))
                .andExpect(model().attribute("errorMessage", "Uid already exists: taken-uid"));
    }

    @Test
    @WithMockUser
    void returnsFormWhenUidInvalid() throws Exception {
        Profile profile = new Profile();
        profile.setUid("current-user");
        when(currentProfileProvider.getCurrentId()).thenReturn(1L);
        when(profileService.findById(1L)).thenReturn(Optional.of(profile));
        doThrow(new IllegalArgumentException("Bad uid"))
                .when(profileService)
                .updateUid(1L, "bad uid");

        mockMvc.perform(post("/account/login")
                        .with(csrf())
                        .param("newUid", "bad uid"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-login"))
                .andExpect(model().attribute("currentUid", "current-user"))
                .andExpect(model().attribute("uidError", "Bad uid"))
                .andExpect(model().attribute("errorMessage", "Bad uid"))
                .andExpect(model().attributeDoesNotExist("uidSuggestions"));
    }

    @Test
    @WithMockUser
    void redirectsOnSuccessfulLoginChange() throws Exception {
        Profile profile = new Profile();
        profile.setUid("current-user");
        when(currentProfileProvider.getCurrentId()).thenReturn(1L);
        when(profileService.findById(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(post("/account/login")
                        .with(csrf())
                        .param("newUid", "new-user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login?loginChanged"));

        verify(profileService).updateUid(1L, "new-user");
    }
}
