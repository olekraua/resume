package net.devstudy.resume.web.controller;

import static net.devstudy.resume.shared.constants.Constants.UI.MAX_PROFILES_PER_PAGE;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.Period;

import net.devstudy.resume.search.api.service.SearchQueryService;
import net.devstudy.resume.search.internal.document.ProfileSearchDocument;
import net.devstudy.resume.web.dto.PageResponse;
import net.devstudy.resume.web.dto.ProfileSummary;

@RestController
@RequiredArgsConstructor
public class SearchApiController {

    private static final int MAX_PAGE_SIZE = 50;

    private final SearchQueryService searchQueryService;

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
        PageRequest pageRequest = PageRequest.of(safePage, safeSize);
        Page<ProfileSearchDocument> result = searchQueryService.search(normalized, pageRequest);
        List<ProfileSummary> items = result.getContent().stream()
                .filter(Objects::nonNull)
                .map(this::toSummary)
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

    private ProfileSummary toSummary(ProfileSearchDocument doc) {
        if (doc == null) {
            return null;
        }
        String fullName = normalizeFullName(doc.getFullName(), doc.getFirstName(), doc.getLastName());
        int age = calculateAge(doc.getBirthDay());
        return new ProfileSummary(
                doc.getUid(),
                fullName,
                age,
                doc.getCity(),
                doc.getCountry(),
                doc.getObjective(),
                doc.getSummary(),
                doc.getSmallPhoto()
        );
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

    private int calculateAge(LocalDate birthDay) {
        if (birthDay == null) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        if (birthDay.isAfter(now)) {
            return 0;
        }
        return Period.between(birthDay, now).getYears();
    }
}
