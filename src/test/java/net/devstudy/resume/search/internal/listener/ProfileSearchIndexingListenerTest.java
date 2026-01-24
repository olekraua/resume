package net.devstudy.resume.search.internal.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.profile.api.model.Skill;
import net.devstudy.resume.profile.api.event.ProfileIndexingRequestedEvent;
import net.devstudy.resume.profile.api.event.ProfileIndexingSnapshot;
import net.devstudy.resume.search.api.service.ProfileSearchService;

class ProfileSearchIndexingListenerTest {

    private final ProfileSearchService profileSearchService = mock(ProfileSearchService.class);

    private final ProfileSearchIndexingListener listener = new ProfileSearchIndexingListener(profileSearchService);

    @Test
    void ignoresNullEvent() {
        listener.onProfileIndexingRequested(null);
        verifyNoInteractions(profileSearchService);
    }

    @Test
    void ignoresNullSnapshot() {
        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(null));
        verifyNoInteractions(profileSearchService);
    }

    @Test
    void ignoresNullProfileId() {
        ProfileIndexingSnapshot snapshot = new ProfileIndexingSnapshot(null, "uid", "first", "last",
                "objective", "summary", "info", List.of("Java"));
        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(snapshot));
        verifyNoInteractions(profileSearchService);
    }

    @Test
    void indexesProfileWhenSnapshotPresent() {
        ProfileIndexingSnapshot snapshot = new ProfileIndexingSnapshot(1L, "uid", "first", "last",
                "objective", "summary", "info", List.of("Java", "Spring"));

        listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(snapshot));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Profile>> captor = ArgumentCaptor.forClass(List.class);
        verify(profileSearchService).indexProfiles(captor.capture());

        Profile profile = captor.getValue().get(0);
        assertEquals(1L, profile.getId());
        assertEquals("uid", profile.getUid());
        assertEquals("first", profile.getFirstName());
        assertEquals("last", profile.getLastName());
        assertEquals("objective", profile.getObjective());
        assertEquals("summary", profile.getSummary());
        assertEquals("info", profile.getInfo());
        assertEquals(List.of("Java", "Spring"),
                profile.getSkills().stream().map(Skill::getValue).toList());
    }

    @Test
    void doesNotThrowWhenIndexingFails() {
        ProfileIndexingSnapshot snapshot = new ProfileIndexingSnapshot(1L, "uid", "first", "last",
                "objective", "summary", "info", List.of("Java"));
        doThrow(new RuntimeException("boom")).when(profileSearchService).indexProfiles(anyList());

        assertDoesNotThrow(() -> listener.onProfileIndexingRequested(new ProfileIndexingRequestedEvent(snapshot)));
    }
}
