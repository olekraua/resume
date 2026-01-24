package net.devstudy.resume.web.controller;

import static net.devstudy.resume.shared.constants.Constants.UI.MAX_PROFILES_PER_PAGE;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.profile.api.service.ProfileService;
import net.devstudy.resume.web.dto.PageResponse;
import net.devstudy.resume.web.dto.ProfileSummary;

@RestController
@RequiredArgsConstructor
public class SearchApiController {

    private static final int MAX_PAGE_SIZE = 50;

    private final ProfileService profileService;

    @GetMapping("/api/search")
    public PageResponse<ProfileSummary> search(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", required = false) Integer size) {
        int safePage = Math.max(0, page);
        int safeSize = normalizeSize(size);
        String normalized = query == null ? "" : query.trim();
        if (normalized.isEmpty()) {
            return new PageResponse<>(List.of(), safePage, safeSize, 0, 0, false);
        }
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("id"));
        Page<Profile> result = profileService.search(normalized, pageRequest);
        List<ProfileSummary> items = result.getContent().stream()
                .filter(Objects::nonNull)
                .map(ProfileSummary::from)
                .filter(Objects::nonNull)
                .toList();
        return new PageResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    private int normalizeSize(Integer size) {
        int effective = size == null ? MAX_PROFILES_PER_PAGE : size;
        return Math.max(1, Math.min(effective, MAX_PAGE_SIZE));
    }
}
