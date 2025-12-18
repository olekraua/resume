package net.devstudy.resume.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.repository.search.ProfileSearchRepository;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.search.ProfileSearchDocument;
import net.devstudy.resume.service.ProfileSearchMapper;

class ProfileSearchServiceImplTest {

    private ProfileRepository profileRepository;
    private ElasticsearchOperations elasticsearchOperations;
    private ProfileSearchServiceImpl service;

    @BeforeEach
    void setUp() {
        profileRepository = Mockito.mock(ProfileRepository.class);
        ProfileSearchRepository profileSearchRepository = Mockito.mock(ProfileSearchRepository.class);
        elasticsearchOperations = Mockito.mock(ElasticsearchOperations.class);
        ProfileSearchMapper profileSearchMapper = Mockito.mock(ProfileSearchMapper.class);
        service = new ProfileSearchServiceImpl(profileRepository, profileSearchRepository, elasticsearchOperations,
                profileSearchMapper);
    }

    @Test
    void searchNullQueryReturnsAllProfilesFromRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> expected = new PageImpl<>(List.of(profile(1L), profile(2L)), pageable, 2);
        Mockito.when(profileRepository.findAll(pageable)).thenReturn(expected);

        Page<Profile> result = service.search(null, pageable);

        assertEquals(expected.getContent(), result.getContent());
        assertEquals(expected.getTotalElements(), result.getTotalElements());
        Mockito.verify(profileRepository).findAll(pageable);
        Mockito.verifyNoInteractions(elasticsearchOperations);
    }

    @Test
    void searchShortQueryAfterTrimReturnsAllProfilesFromRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> expected = new PageImpl<>(List.of(profile(1L)), pageable, 1);
        Mockito.when(profileRepository.findAll(pageable)).thenReturn(expected);

        Page<Profile> result = service.search(" a ", pageable);

        assertEquals(expected.getContent(), result.getContent());
        Mockito.verify(profileRepository).findAll(pageable);
        Mockito.verifyNoInteractions(elasticsearchOperations);
    }

    @Test
    void searchReturnsEmptyPageWhenElasticsearchReturnsNoHits() {
        Pageable pageable = PageRequest.of(0, 10);
        @SuppressWarnings("unchecked")
        SearchHits<ProfileSearchDocument> hits = Mockito.mock(SearchHits.class);
        Mockito.when(hits.isEmpty()).thenReturn(true);
        Mockito.when(elasticsearchOperations.search(any(Query.class), eq(ProfileSearchDocument.class))).thenReturn(hits);

        Page<Profile> result = service.search("java", pageable);

        assertTrue(result.isEmpty());
        Mockito.verify(elasticsearchOperations).search(any(Query.class), eq(ProfileSearchDocument.class));
        Mockito.verify(profileRepository, never()).findAll(pageable);
        Mockito.verify(profileRepository, never()).findAllById(any());
    }

    @Test
    void searchOrdersProfilesByIdsFromElasticsearchHits() {
        Pageable pageable = PageRequest.of(0, 10);
        @SuppressWarnings("unchecked")
        SearchHits<ProfileSearchDocument> hits = Mockito.mock(SearchHits.class);
        Mockito.when(hits.isEmpty()).thenReturn(false);
        Mockito.when(hits.getTotalHits()).thenReturn(3L);

        @SuppressWarnings("unchecked")
        SearchHit<ProfileSearchDocument> hit1 = Mockito.mock(SearchHit.class);
        @SuppressWarnings("unchecked")
        SearchHit<ProfileSearchDocument> hit2 = Mockito.mock(SearchHit.class);
        @SuppressWarnings("unchecked")
        SearchHit<ProfileSearchDocument> hit3 = Mockito.mock(SearchHit.class);

        Mockito.when(hit1.getContent())
                .thenReturn(new ProfileSearchDocument(3L, null, null, null, null, null, null, null, null));
        Mockito.when(hit2.getContent())
                .thenReturn(new ProfileSearchDocument(1L, null, null, null, null, null, null, null, null));
        Mockito.when(hit3.getContent())
                .thenReturn(new ProfileSearchDocument(2L, null, null, null, null, null, null, null, null));
        Mockito.when(hits.getSearchHits()).thenReturn(List.of(hit1, hit2, hit3));

        Mockito.when(elasticsearchOperations.search(any(Query.class), eq(ProfileSearchDocument.class))).thenReturn(hits);

        Profile p1 = profile(1L);
        Profile p2 = profile(2L);
        Profile p3 = profile(3L);
        Mockito.when(profileRepository.findAllById(List.of(3L, 1L, 2L))).thenReturn(List.of(p2, p3, p1));

        Page<Profile> result = service.search("java", pageable);

        assertEquals(List.of(p3, p1, p2), result.getContent());
        assertEquals(3L, result.getTotalElements());
        Mockito.verify(profileRepository).findAllById(List.of(3L, 1L, 2L));
        Mockito.verify(profileRepository, never()).findAll(pageable);
        Mockito.verify(profileRepository, never()).search(Mockito.anyString(), Mockito.any(Pageable.class));
    }

    private static Profile profile(long id) {
        Profile profile = new Profile();
        profile.setId(id);
        return profile;
    }
}
