package net.devstudy.resume.search.internal.web;

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

import net.devstudy.resume.search.api.service.SearchQueryService;
import net.devstudy.resume.search.internal.document.ProfileSearchDocument;

class SearchQueryApiControllerTest {

    @Test
    void suggestReturnsEmptyWhenQueryNull() {
        SearchQueryService searchQueryService = mock(SearchQueryService.class);
        SearchQueryApiController controller = new SearchQueryApiController(searchQueryService);

        List<SearchQueryApiController.SuggestItem> result = controller.suggest(null, 5);

        assertTrue(result.isEmpty());
        verifyNoInteractions(searchQueryService);
    }

    @Test
    void suggestReturnsEmptyWhenQueryBlank() {
        SearchQueryService searchQueryService = mock(SearchQueryService.class);
        SearchQueryApiController controller = new SearchQueryApiController(searchQueryService);

        List<SearchQueryApiController.SuggestItem> result = controller.suggest("   ", 5);

        assertTrue(result.isEmpty());
        verify(searchQueryService, never()).search(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void suggestUsesMinimumLimitAndMapsNullFullName() {
        SearchQueryService searchQueryService = mock(SearchQueryService.class);
        SearchQueryApiController controller = new SearchQueryApiController(searchQueryService);

        ProfileSearchDocument doc = new ProfileSearchDocument(1L, "uid-1", "John", "Doe", null,
                null, null, null, null, null, null, null, null);
        Page<ProfileSearchDocument> page = new PageImpl<>(List.of(doc));
        when(searchQueryService.search("java", PageRequest.of(0, 1))).thenReturn(page);

        List<SearchQueryApiController.SuggestItem> result = controller.suggest("  java  ", 0);

        assertEquals(1, result.size());
        assertEquals("uid-1", result.getFirst().uid());
        assertEquals("John Doe", result.getFirst().fullName());
        verify(searchQueryService).search("java", PageRequest.of(0, 1));
    }

    @Test
    void suggestCapsLimitAt50AndTrimsFullName() {
        SearchQueryService searchQueryService = mock(SearchQueryService.class);
        SearchQueryApiController controller = new SearchQueryApiController(searchQueryService);

        ProfileSearchDocument doc = new ProfileSearchDocument(2L, "uid-2", "John", "Doe",
                "  John Doe  ", null, null, null, null, null, null, null, null);
        Page<ProfileSearchDocument> page = new PageImpl<>(List.of(doc));
        when(searchQueryService.search("john", PageRequest.of(0, 50))).thenReturn(page);

        List<SearchQueryApiController.SuggestItem> result = controller.suggest("john", 100);

        assertEquals(1, result.size());
        assertEquals("uid-2", result.getFirst().uid());
        assertEquals("John Doe", result.getFirst().fullName());
        verify(searchQueryService).search("john", PageRequest.of(0, 50));
    }

    @Test
    void suggestUsesProvidedLimitWithinRange() {
        SearchQueryService searchQueryService = mock(SearchQueryService.class);
        SearchQueryApiController controller = new SearchQueryApiController(searchQueryService);

        Page<ProfileSearchDocument> page = new PageImpl<>(List.of());
        when(searchQueryService.search("q", PageRequest.of(0, 7))).thenReturn(page);

        List<SearchQueryApiController.SuggestItem> result = controller.suggest("q", 7);

        assertTrue(result.isEmpty());
        verify(searchQueryService).search("q", PageRequest.of(0, 7));
    }
}
