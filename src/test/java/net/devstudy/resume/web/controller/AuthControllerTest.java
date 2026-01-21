package net.devstudy.resume.web.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import net.devstudy.resume.shared.component.DataBuilder;
import net.devstudy.resume.auth.component.impl.AccessDeniedHandlerImpl;
import net.devstudy.resume.auth.config.SecurityConfig;
import net.devstudy.resume.web.config.UiModelAttributes;
import net.devstudy.resume.web.config.UiProperties;
import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.profile.exception.UidAlreadyExistsException;
import net.devstudy.resume.auth.model.CurrentProfile;
import net.devstudy.resume.auth.security.CurrentProfileProvider;
import net.devstudy.resume.profile.service.ProfileService;
import net.devstudy.resume.auth.service.UidSuggestionService;

@WebMvcTest(controllers = AuthController.class)
@Import({UiProperties.class, UiModelAttributes.class, SecurityConfig.class, AccessDeniedHandlerImpl.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
@TestPropertySource(properties = "spring.messages.basename=i18n.messages")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private CurrentProfileProvider currentProfileProvider;

    @MockitoBean
    private UidSuggestionService uidSuggestionService;

    @MockitoBean
    private DataBuilder dataBuilder;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void redirectsToMeWhenAlreadyLoggedIn() throws Exception {
        when(currentProfileProvider.getCurrentProfile()).thenReturn(buildCurrentProfile("owner-user"));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .with(user(buildCurrentProfile("owner-user")))
                        .param("uid", "new-user")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("password", "password")
                        .param("confirmPassword", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/me"));

        verify(profileService, never())
                .register(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void returnsFormWhenBindingErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registrationForm",
                        "uid", "firstName", "lastName", "password", "confirmPassword"));

        verify(profileService, never())
                .register(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void registersAndRedirectsToProfile() throws Exception {
        Profile profile = new Profile();
        profile.setUid("john-doe");
        profile.setPassword("password");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.register("john-doe", "John", "Doe", "password"))
                .thenReturn(profile);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("uid", "john-doe")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("password", "password")
                        .param("confirmPassword", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/john-doe"));

        verify(profileService).register("john-doe", "John", "Doe", "password");
    }

    @Test
    void returnsFormWhenUidAlreadyExists() throws Exception {
        doThrow(new UidAlreadyExistsException("taken-uid"))
                .when(profileService)
                .register("taken-uid", "John", "Doe", "password");
        when(uidSuggestionService.suggest("taken-uid"))
                .thenReturn(List.of("taken-uid1"));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("uid", "taken-uid")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("password", "password")
                        .param("confirmPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "uid"))
                .andExpect(model().attribute("uidSuggestions", hasItem("taken-uid1")))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    void returnsFormWhenIllegalArgumentExceptionThrown() throws Exception {
        doThrow(new IllegalArgumentException("Bad input"))
                .when(profileService)
                .register("bad-uid", "John", "Doe", "password");

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("uid", "bad-uid")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("password", "password")
                        .param("confirmPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("errorMessage", is("Bad input")))
                .andExpect(model().attributeDoesNotExist("uidSuggestions"));
    }

    @Test
    void redirectsToLoginWhenCurrentProfileMissing() throws Exception {
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        mockMvc.perform(get("/me")
                        .with(user(buildCurrentProfile("owner-user"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void redirectsToProfileWhenCurrentProfilePresent() throws Exception {
        when(currentProfileProvider.getCurrentProfile()).thenReturn(buildCurrentProfile("john-doe"));

        mockMvc.perform(get("/me")
                        .with(user(buildCurrentProfile("owner-user"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/john-doe"));
    }

    @Test
    void returnsUidHintWithBothNames() throws Exception {
        when(dataBuilder.buildProfileUid("John", "Doe")).thenReturn("john-doe");

        mockMvc.perform(get("/register/uid-hint")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(content().string("john-doe"));

        verify(dataBuilder).buildProfileUid("John", "Doe");
    }

    @Test
    void returnsUidHintWhenParamsMissing() throws Exception {
        when(dataBuilder.buildProfileUid(null, null)).thenReturn("");

        mockMvc.perform(get("/register/uid-hint"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(dataBuilder).buildProfileUid(null, null);
    }

    @Test
    void redirectsToMeWhenRegisterFormRequestedByLoggedInUser() throws Exception {
        when(currentProfileProvider.getCurrentProfile()).thenReturn(buildCurrentProfile("owner-user"));

        mockMvc.perform(get("/register")
                        .with(user(buildCurrentProfile("owner-user"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/me"));
    }

    @Test
    void rendersRegisterFormWhenUserNotLoggedIn() throws Exception {
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void rendersLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void redirectsToMeWhenLoginFormRequestedByLoggedInUser() throws Exception {
        when(currentProfileProvider.getCurrentProfile()).thenReturn(buildCurrentProfile("owner-user"));

        mockMvc.perform(get("/login")
                        .with(user(buildCurrentProfile("owner-user"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/me"));
    }

    private CurrentProfile buildCurrentProfile(String uid) {
        Profile profile = new Profile();
        profile.setUid(uid);
        profile.setPassword("password");
        profile.setFirstName("Test");
        profile.setLastName("User");
        return new CurrentProfile(profile);
    }
}
