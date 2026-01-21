package net.devstudy.resume.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.search.service.ProfileSearchService;

class SuggestControllerTest {

    @Test
    void suggestReturnsEmptyWhenQueryNull() {
        ProfileSearchService profileSearchService = mock(ProfileSearchService.class);
        SuggestController controller = new SuggestController(profileSearchService);

        List<SuggestController.SuggestItem> result = controller.suggest(null, 5);

        assertTrue(result.isEmpty());
        verifyNoInteractions(profileSearchService);
    }

    @Test
    void suggestReturnsEmptyWhenQueryBlank() {
        ProfileSearchService profileSearchService = mock(ProfileSearchService.class);
        SuggestController controller = new SuggestController(profileSearchService);

        List<SuggestController.SuggestItem> result = controller.suggest("   ", 5);

        assertTrue(result.isEmpty());
        verify(profileSearchService, never()).search(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void suggestUsesMinimumLimitAndMapsNullFullName() {
        ProfileSearchService profileSearchService = mock(ProfileSearchService.class);
        SuggestController controller = new SuggestController(profileSearchService);

        Profile profile = mock(Profile.class);
        when(profile.getUid()).thenReturn("uid-1");
        when(profile.getFullName()).thenReturn(null);
        Page<Profile> page = new PageImpl<>(List.of(profile));
        when(profileSearchService.search("java", PageRequest.of(0, 1))).thenReturn(page);

        List<SuggestController.SuggestItem> result = controller.suggest("  java  ", 0);

        assertEquals(1, result.size());
        assertEquals("uid-1", result.getFirst().uid());
        assertEquals("", result.getFirst().fullName());
        verify(profileSearchService).search("java", PageRequest.of(0, 1));
    }

    @Test
    void suggestCapsLimitAt50AndTrimsFullName() {
        ProfileSearchService profileSearchService = mock(ProfileSearchService.class);
        SuggestController controller = new SuggestController(profileSearchService);

        Profile profile = mock(Profile.class);
        when(profile.getUid()).thenReturn("uid-2");
        when(profile.getFullName()).thenReturn("  John Doe  ");
        Page<Profile> page = new PageImpl<>(List.of(profile));
        when(profileSearchService.search("john", PageRequest.of(0, 50))).thenReturn(page);

        List<SuggestController.SuggestItem> result = controller.suggest("john", 100);

        assertEquals(1, result.size());
        assertEquals("uid-2", result.getFirst().uid());
        assertEquals("John Doe", result.getFirst().fullName());
        verify(profileSearchService).search("john", PageRequest.of(0, 50));
    }

    @Test
    void suggestUsesProvidedLimitWithinRange() {
        ProfileSearchService profileSearchService = mock(ProfileSearchService.class);
        SuggestController controller = new SuggestController(profileSearchService);

        Page<Profile> page = new PageImpl<>(List.of());
        when(profileSearchService.search("q", PageRequest.of(0, 7))).thenReturn(page);

        List<SuggestController.SuggestItem> result = controller.suggest("q", 7);

        assertTrue(result.isEmpty());
        verify(profileSearchService).search("q", PageRequest.of(0, 7));
    }
}
