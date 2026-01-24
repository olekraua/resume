package net.devstudy.resume.search.internal.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.search.api.event.ProfileIndexingRequestedEvent;
import net.devstudy.resume.profile.api.service.ProfileReadService;
import net.devstudy.resume.search.api.service.ProfileSearchService;

class ProfileSearchIndexingListenerTest {

    private final ProfileReadService profileReadService = mock(ProfileReadService.class);
    private final ProfileSearchService profileSearchService = mock(ProfileSearchService.class);

    private final ProfileSearchIndexingListener listener = new ProfileSearchIndexingListener(profileReadService,
            profileSearchService);

    @Test
    void ignoresNullEvent() {
        listener.onProfileIndexingRequested(null);
        verifyNoInteractions(profileReadService, profileSearchService);
    }

    @Test
    void ignoresNullProfileId() {
        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(null));
        verifyNoInteractions(profileReadService, profileSearchService);
    }

    @Test
    void doesNothingWhenProfileNotFound() {
        when(profileReadService.findById(1L)).thenReturn(Optional.empty());

        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(1L));

        verify(profileReadService).findById(1L);
        verifyNoInteractions(profileSearchService);
    }

    @Test
    void indexesProfileWhenFound() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setSkills(List.of());
        when(profileReadService.findById(1L)).thenReturn(Optional.of(profile));

        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(1L));

        verify(profileSearchService).indexProfiles(List.of(profile));
    }

    @Test
    void doesNotThrowWhenIndexingFails() {
        Profile profile = new Profile();
        profile.setId(1L);
        when(profileReadService.findById(1L)).thenReturn(Optional.of(profile));
        doThrow(new RuntimeException("boom")).when(profileSearchService).indexProfiles(List.of(profile));

        assertDoesNotThrow(() -> listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(1L)));
    }
}
