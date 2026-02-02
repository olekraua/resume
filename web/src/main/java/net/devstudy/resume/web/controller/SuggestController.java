package net.devstudy.resume.web.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.search.api.service.SearchQueryService;
import net.devstudy.resume.search.internal.document.ProfileSearchDocument;

@RestController
@RequiredArgsConstructor
public class SuggestController {

    private final SearchQueryService searchQueryService;

    @GetMapping("/api/suggest")
    public List<SuggestItem> suggest(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {

        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        int size = Math.max(1, Math.min(limit, 50));

        return searchQueryService.search(query.trim(), PageRequest.of(0, size))
                .getContent().stream()
                .map(this::toDto)
                .toList();
    }

    private SuggestItem toDto(ProfileSearchDocument doc) {
        String fullName = normalizeFullName(doc.getFullName(), doc.getFirstName(), doc.getLastName());
        return new SuggestItem(doc.getUid(), fullName);
    }

    private String normalizeFullName(String fullName, String firstName, String lastName) {
        if (fullName != null && !fullName.isBlank()) {
            return fullName.trim();
        }
        String combined = String.format("%s %s",
                firstName == null ? "" : firstName.trim(),
                lastName == null ? "" : lastName.trim()).trim();
        return combined;
    }

    public record SuggestItem(String uid, String fullName) {
    }
}
