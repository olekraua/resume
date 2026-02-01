package net.devstudy.resume.search.api.dto;

import java.time.LocalDate;
import java.time.Period;

import net.devstudy.resume.search.internal.document.ProfileSearchDocument;

public record ProfileSummary(
        String uid,
        String fullName,
        int age,
        String city,
        String country,
        String objective,
        String summary,
        String smallPhoto
) {
    public static ProfileSummary from(ProfileSearchDocument doc) {
        if (doc == null) {
            return null;
        }
        String fullName = doc.getFullName();
        if (fullName == null) {
            fullName = "";
        } else {
            fullName = fullName.trim();
        }
        return new ProfileSummary(
                doc.getUid(),
                fullName,
                calculateAge(doc.getBirthDay()),
                doc.getCity(),
                doc.getCountry(),
                doc.getObjective(),
                doc.getSummary(),
                doc.getSmallPhoto()
        );
    }

    private static int calculateAge(LocalDate birthDay) {
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
