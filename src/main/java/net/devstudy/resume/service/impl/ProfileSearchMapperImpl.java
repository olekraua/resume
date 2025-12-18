package net.devstudy.resume.service.impl;

import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.Skill;
import net.devstudy.resume.search.ProfileSearchDocument;
import net.devstudy.resume.service.ProfileSearchMapper;

@Component
public class ProfileSearchMapperImpl implements ProfileSearchMapper {

    static final int MAX_NAME_LENGTH = 64;
    static final int MAX_FULL_NAME_LENGTH = 130;
    static final int MAX_OBJECTIVE_LENGTH = 5000;
    static final int MAX_SUMMARY_LENGTH = 5000;
    static final int MAX_INFO_LENGTH = 5000;
    static final int MAX_SKILL_VALUE_LENGTH = 128;
    static final int MAX_SKILLS_LENGTH = 2000;

    @Override
    public ProfileSearchDocument toDocument(Profile profile) {
        String first = normalizeText(profile.getFirstName(), MAX_NAME_LENGTH);
        String last = normalizeText(profile.getLastName(), MAX_NAME_LENGTH);
        String fullName = truncate((first + " " + last).trim(), MAX_FULL_NAME_LENGTH);
        String skills = extractSkills(profile.getSkills());
        return new ProfileSearchDocument(profile.getId(), profile.getUid(), first, last, fullName,
                normalizeText(profile.getObjective(), MAX_OBJECTIVE_LENGTH),
                normalizeText(profile.getSummary(), MAX_SUMMARY_LENGTH),
                normalizeText(profile.getInfo(), MAX_INFO_LENGTH),
                skills);
    }

    private String extractSkills(java.util.List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        String joined = skills.stream()
                .map(Skill::getValue)
                .map(value -> normalizeText(value, MAX_SKILL_VALUE_LENGTH))
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
        return truncate(joined, MAX_SKILLS_LENGTH);
    }

    private String normalizeText(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String text = Jsoup.parse(value).text();
        String normalized = text.replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        return truncate(normalized, maxLength);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int codePoints = value.codePointCount(0, value.length());
        if (codePoints <= maxLength) {
            return value;
        }
        int endIndex = value.offsetByCodePoints(0, maxLength);
        return value.substring(0, endIndex);
    }
}
