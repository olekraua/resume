package net.devstudy.resume.search;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.event.ProfileIndexingRequestedEvent;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.service.ProfileSearchService;

class ProfileSearchIndexingListenerTest {

    private final ProfileRepository profileRepository = mock(ProfileRepository.class);
    private final ProfileSearchService profileSearchService = mock(ProfileSearchService.class);

    private final ProfileSearchIndexingListener listener = new ProfileSearchIndexingListener(profileRepository,
            profileSearchService);

    @Test
    void ignoresNullEvent() {
        listener.onProfileIndexingRequested(null);
        verifyNoInteractions(profileRepository, profileSearchService);
    }

    @Test
    void ignoresNullProfileId() {
        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(null));
        verifyNoInteractions(profileRepository, profileSearchService);
    }

    @Test
    void doesNothingWhenProfileNotFound() {
        when(profileRepository.findById(1L)).thenReturn(Optional.empty());

        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(1L));

        verify(profileRepository).findById(1L);
        verifyNoInteractions(profileSearchService);
    }

    @Test
    void indexesProfileWhenFound() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setSkills(List.of());
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(1L));

        verify(profileSearchService).indexProfiles(List.of(profile));
    }

    @Test
    void doesNotThrowWhenIndexingFails() {
        Profile profile = new Profile();
        profile.setId(1L);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        doThrow(new RuntimeException("boom")).when(profileSearchService).indexProfiles(List.of(profile));

        assertDoesNotThrow(() -> listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(1L)));
    }
}

