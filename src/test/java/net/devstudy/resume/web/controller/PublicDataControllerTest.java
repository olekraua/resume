package net.devstudy.resume.web.controller;

import static net.devstudy.resume.shared.constants.Constants.UI.MAX_PROFILES_PER_PAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ExtendedModelMap;

import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.auth.form.ChangeLoginForm;
import net.devstudy.resume.auth.model.CurrentProfile;
import net.devstudy.resume.auth.security.CurrentProfileProvider;
import net.devstudy.resume.profile.service.ProfileService;

class PublicDataControllerTest {

    @Test
    void redirectToWelcomeReturnsRedirect() {
        PublicDataController controller = new PublicDataController(
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        assertEquals("redirect:/welcome", controller.redirectToWelcome());
    }

    @Test
    void listAllUsesFindAllWhenQueryMissing() {
        ProfileService profileService = mock(ProfileService.class);

        PublicDataController controller = new PublicDataController(
                profileService,
                mock(CurrentProfileProvider.class));

        int pageNumber = 1;
        PageRequest pageRequest = PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id"));
        Profile profile = new Profile();
        Page<Profile> page = new PageImpl<>(List.of(profile), pageRequest, 1);
        when(profileService.findAll(pageRequest)).thenReturn(page);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.listAll(model, pageNumber, null);

        assertEquals("welcome", view);
        assertEquals(page.getContent(), model.get("profiles"));
        assertSame(page, model.get("page"));
        assertEquals("", model.get("query"));
        verify(profileService).findAll(pageRequest);
        verify(profileService, never()).search(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void listAllUsesSearchWhenQueryPresent() {
        ProfileService profileService = mock(ProfileService.class);

        PublicDataController controller = new PublicDataController(
                profileService,
                mock(CurrentProfileProvider.class));

        int pageNumber = 2;
        String query = "  John  ";
        String trimmed = "John";
        PageRequest pageRequest = PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id"));
        Profile profile = new Profile();
        Page<Profile> page = new PageImpl<>(List.of(profile), pageRequest, 1);
        when(profileService.search(trimmed, pageRequest)).thenReturn(page);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.listAll(model, pageNumber, query);

        assertEquals("welcome", view);
        assertEquals(page.getContent(), model.get("profiles"));
        assertSame(page, model.get("page"));
        assertEquals(trimmed, model.get("query"));
        verify(profileService).search(trimmed, pageRequest);
        verify(profileService, never()).findAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void loadMoreProfilesUsesFindAllWhenQueryMissing() {
        ProfileService profileService = mock(ProfileService.class);

        PublicDataController controller = new PublicDataController(
                profileService,
                mock(CurrentProfileProvider.class));

        int pageNumber = 0;
        PageRequest pageRequest = PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id"));
        Profile profile = new Profile();
        Page<Profile> page = new PageImpl<>(List.of(profile), pageRequest, 1);
        when(profileService.findAll(pageRequest)).thenReturn(page);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.loadMoreProfiles(pageNumber, null, model);

        assertEquals("profiles :: items", view);
        assertEquals(page.getContent(), model.get("profiles"));
        assertEquals("", model.get("query"));
        verify(profileService).findAll(pageRequest);
        verify(profileService, never()).search(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void loadMoreProfilesUsesSearchWhenQueryPresent() {
        ProfileService profileService = mock(ProfileService.class);

        PublicDataController controller = new PublicDataController(
                profileService,
                mock(CurrentProfileProvider.class));

        int pageNumber = 3;
        String query = "  Doe ";
        String trimmed = "Doe";
        PageRequest pageRequest = PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id"));
        Profile profile = new Profile();
        Page<Profile> page = new PageImpl<>(List.of(profile), pageRequest, 1);
        when(profileService.search(trimmed, pageRequest)).thenReturn(page);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.loadMoreProfiles(pageNumber, query, model);

        assertEquals("profiles :: items", view);
        assertEquals(page.getContent(), model.get("profiles"));
        assertEquals(trimmed, model.get("query"));
        verify(profileService).search(trimmed, pageRequest);
        verify(profileService, never()).findAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void profileReturnsNotFoundWhenMissing() {
        ProfileService profileService = mock(ProfileService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        PublicDataController controller = new PublicDataController(profileService, currentProfileProvider);

        String uid = "missing";
        when(profileService.findWithAllByUid(uid)).thenReturn(Optional.empty());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.profile(uid, model);

        assertEquals("error/profile-not-found", view);
        verify(profileService).findWithAllByUid(uid);
        verify(currentProfileProvider, never()).getCurrentProfile();
    }

    @Test
    void profileSetsOwnProfileFalseWhenNotOwner() {
        ProfileService profileService = mock(ProfileService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        PublicDataController controller = new PublicDataController(profileService, currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("secret");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findWithAllByUid(profile.getUid())).thenReturn(Optional.of(profile));
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.profile(profile.getUid(), model);

        assertEquals("profile", view);
        assertSame(profile, model.get("profile"));
        assertEquals(Boolean.FALSE, model.get("ownProfile"));
        assertNull(model.get("changeLoginForm"));
        assertNull(model.get("currentUid"));
    }

    @Test
    void profileSetsOwnProfileTrueWhenOwner() {
        ProfileService profileService = mock(ProfileService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        PublicDataController controller = new PublicDataController(profileService, currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(7L);
        profile.setUid("owner-user");
        profile.setPassword("secret");
        profile.setFirstName("Owner");
        profile.setLastName("User");
        when(profileService.findWithAllByUid(profile.getUid())).thenReturn(Optional.of(profile));
        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.profile(profile.getUid(), model);

        assertEquals("profile", view);
        assertSame(profile, model.get("profile"));
        assertEquals(Boolean.TRUE, model.get("ownProfile"));
        ChangeLoginForm form = (ChangeLoginForm) model.get("changeLoginForm");
        assertEquals(profile.getUid(), form.getNewUid());
        assertEquals(profile.getUid(), model.get("currentUid"));
    }

    @Test
    void searchRedirectsWhenQueryNull() {
        ProfileService profileService = mock(ProfileService.class);
        PublicDataController controller = new PublicDataController(
                profileService,
                mock(CurrentProfileProvider.class));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.search(null, 0, model);

        assertEquals("redirect:/welcome", view);
        verify(profileService, never()).search(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void searchRedirectsWhenQueryBlank() {
        ProfileService profileService = mock(ProfileService.class);
        PublicDataController controller = new PublicDataController(
                profileService,
                mock(CurrentProfileProvider.class));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.search("   ", 0, model);

        assertEquals("redirect:/welcome", view);
        verify(profileService, never()).search(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void searchReturnsResultsWhenQueryPresent() {
        ProfileService profileService = mock(ProfileService.class);
        PublicDataController controller = new PublicDataController(
                profileService,
                mock(CurrentProfileProvider.class));

        int pageNumber = 1;
        String query = "  java ";
        String trimmed = "java";
        PageRequest pageRequest = PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id"));
        Profile profile = new Profile();
        Page<Profile> page = new PageImpl<>(List.of(profile), pageRequest, 1);
        when(profileService.search(trimmed, pageRequest)).thenReturn(page);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.search(query, pageNumber, model);

        assertEquals("search-results", view);
        assertEquals(page.getContent(), model.get("profiles"));
        assertSame(page, model.get("page"));
        assertEquals(trimmed, model.get("query"));
        verify(profileService).search(trimmed, pageRequest);
    }
}
