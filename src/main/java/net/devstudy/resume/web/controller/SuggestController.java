package net.devstudy.resume.web.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.search.service.ProfileSearchService;

@RestController
@RequiredArgsConstructor
public class SuggestController {

    private final ProfileSearchService profileSearchService;

    @GetMapping("/api/suggest")
    public List<SuggestItem> suggest(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {

        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        int size = Math.max(1, Math.min(limit, 50));

        return profileSearchService.search(query.trim(), PageRequest.of(0, size))
                .getContent().stream()
                .map(this::toDto)
                .toList();
    }

    private SuggestItem toDto(Profile profile) {
        String fullName = profile.getFullName();
        return new SuggestItem(profile.getUid(), fullName == null ? "" : fullName.trim());
    }

    public record SuggestItem(String uid, String fullName) {
    }
}
