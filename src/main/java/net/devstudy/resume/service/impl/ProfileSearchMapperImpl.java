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

    @Override
    public ProfileSearchDocument toDocument(Profile profile) {
        String first = normalizeText(profile.getFirstName());
        String last = normalizeText(profile.getLastName());
        String fullName = (first + " " + last).trim();
        String skills = extractSkills(profile.getSkills());
        return new ProfileSearchDocument(profile.getId(), profile.getUid(), first, last, fullName,
                normalizeText(profile.getObjective()),
                normalizeText(profile.getSummary()),
                normalizeText(profile.getInfo()),
                skills);
    }

    private String extractSkills(java.util.List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        return skills.stream()
                .map(Skill::getValue)
                .map(this::normalizeText)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String text = Jsoup.parse(value).text();
        return text.replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
