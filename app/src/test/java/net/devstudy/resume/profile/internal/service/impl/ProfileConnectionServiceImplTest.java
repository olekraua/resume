package net.devstudy.resume.profile.internal.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.profile.api.model.ProfileConnection;
import net.devstudy.resume.profile.api.model.ProfileConnectionState;
import net.devstudy.resume.profile.api.model.ProfileConnectionStatus;
import net.devstudy.resume.profile.internal.repository.storage.ProfileConnectionRepository;
import net.devstudy.resume.profile.internal.repository.storage.ProfileRepository;

class ProfileConnectionServiceImplTest {

    private ProfileConnectionServiceImpl service;
    private ProfileConnectionRepository connectionRepository;
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        connectionRepository = Mockito.mock(ProfileConnectionRepository.class);
        profileRepository = Mockito.mock(ProfileRepository.class);
        service = new ProfileConnectionServiceImpl(connectionRepository, profileRepository);
    }

    @Test
    void requestConnectionCreatesNewPendingRequest() {
        Profile requester = profile(1L);
        Profile addressee = profile(2L);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(addressee));
        when(connectionRepository.findByPairKeyForUpdate("1:2")).thenReturn(Optional.empty());
        when(connectionRepository.save(any(ProfileConnection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileConnection created = service.requestConnection(1L, 2L);

        assertEquals(ProfileConnectionStatus.PENDING, created.getStatus());
        assertEquals("1:2", created.getPairKey());
        assertNotNull(created.getCreated());
        assertEquals(requester, created.getRequester());
        assertEquals(addressee, created.getAddressee());
    }

    @Test
    void requestConnectionReturnsExistingWhenOutgoingPending() {
        Profile requester = profile(1L);
        Profile addressee = profile(2L);
        ProfileConnection existing = new ProfileConnection(requester, addressee,
                ProfileConnectionStatus.PENDING, java.time.Instant.now());
        existing.setPairKey("1:2");
        when(profileRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(addressee));
        when(connectionRepository.findByPairKeyForUpdate("1:2")).thenReturn(Optional.of(existing));

        ProfileConnection result = service.requestConnection(1L, 2L);

        assertEquals(existing, result);
        verify(connectionRepository, never()).save(any(ProfileConnection.class));
    }

    @Test
    void requestConnectionRejectsWhenIncomingPending() {
        Profile requester = profile(2L);
        Profile addressee = profile(1L);
        ProfileConnection existing = new ProfileConnection(requester, addressee,
                ProfileConnectionStatus.PENDING, java.time.Instant.now());
        existing.setPairKey("1:2");
        when(profileRepository.findById(1L)).thenReturn(Optional.of(addressee));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(connectionRepository.findByPairKeyForUpdate("1:2")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> service.requestConnection(1L, 2L));
        verify(connectionRepository, never()).save(any(ProfileConnection.class));
    }

    @Test
    void acceptRequestMarksAccepted() {
        Profile requester = profile(2L);
        Profile addressee = profile(1L);
        ProfileConnection existing = new ProfileConnection(requester, addressee,
                ProfileConnectionStatus.PENDING, java.time.Instant.now());
        existing.setPairKey("1:2");
        when(connectionRepository.findByPairKeyForUpdate("1:2")).thenReturn(Optional.of(existing));
        when(connectionRepository.save(existing)).thenReturn(existing);

        ProfileConnection result = service.acceptRequest(1L, 2L);

        assertEquals(ProfileConnectionStatus.ACCEPTED, result.getStatus());
        assertNotNull(result.getResponded());
    }

    @Test
    void declineRequestDeletesPending() {
        Profile requester = profile(2L);
        Profile addressee = profile(1L);
        ProfileConnection existing = new ProfileConnection(requester, addressee,
                ProfileConnectionStatus.PENDING, java.time.Instant.now());
        existing.setPairKey("1:2");
        when(connectionRepository.findByPairKeyForUpdate("1:2")).thenReturn(Optional.of(existing));

        service.declineRequest(1L, 2L);

        verify(connectionRepository).delete(existing);
    }

    @Test
    void removeConnectionDeletesAccepted() {
        Profile requester = profile(1L);
        Profile addressee = profile(2L);
        ProfileConnection existing = new ProfileConnection(requester, addressee,
                ProfileConnectionStatus.ACCEPTED, java.time.Instant.now());
        existing.setPairKey("1:2");
        when(connectionRepository.findByPairKeyForUpdate("1:2")).thenReturn(Optional.of(existing));

        service.removeConnection(1L, 2L);

        verify(connectionRepository).delete(existing);
    }

    @Test
    void getConnectionStateReturnsOutgoingForPending() {
        Profile requester = profile(1L);
        Profile addressee = profile(2L);
        ProfileConnection existing = new ProfileConnection(requester, addressee,
                ProfileConnectionStatus.PENDING, java.time.Instant.now());
        existing.setPairKey("1:2");
        when(connectionRepository.findByPairKey("1:2")).thenReturn(Optional.of(existing));

        ProfileConnectionState state = service.getConnectionState(1L, 2L);

        assertEquals(ProfileConnectionState.OUTGOING_REQUEST, state);
    }

    private Profile profile(Long id) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setFirstName("First");
        profile.setLastName("Last");
        profile.setUid("user-" + id);
        profile.setPassword("secret");
        profile.setCompleted(true);
        return profile;
    }
}
