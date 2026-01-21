package net.devstudy.resume.search.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.profile.entity.Skill;
import net.devstudy.resume.search.ProfileSearchDocument;

class ProfileSearchMapperImplTest {

    private final ProfileSearchMapperImpl mapper = new ProfileSearchMapperImpl(true, true);

    @Test
    void toDocumentStripsHtmlAndNormalizesWhitespace() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john_doe");
        profile.setFirstName(" <b>John</b> ");
        profile.setLastName("\nDoe\t");
        profile.setObjective("<p>Hello&nbsp;&nbsp;world</p>");
        profile.setSummary("<div>Senior<br>Java</div>");
        profile.setInfo("<p>About <i>me</i></p>");

        Skill s1 = new Skill();
        s1.setValue("<b>Java</b>");
        Skill s2 = new Skill();
        s2.setValue("   ");
        Skill s3 = new Skill();
        s3.setValue("<i>Spring</i>");
        profile.setSkills(List.of(s1, s2, s3));

        ProfileSearchDocument doc = mapper.toDocument(profile);

        assertEquals("John", doc.getFirstName());
        assertEquals("Doe", doc.getLastName());
        assertEquals("John Doe", doc.getFullName());
        assertEquals("Hello world", doc.getObjective());
        assertEquals("Senior Java", doc.getSummary());
        assertEquals("About me", doc.getInfo());
        assertEquals("Java, Spring", doc.getSkills());
    }

    @Test
    void toDocumentTruncatesLongFields() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john_doe");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setInfo("a".repeat(ProfileSearchMapperImpl.MAX_INFO_LENGTH + 10));

        ProfileSearchDocument doc = mapper.toDocument(profile);

        assertEquals(ProfileSearchMapperImpl.MAX_INFO_LENGTH, doc.getInfo().length());
        assertEquals("a".repeat(ProfileSearchMapperImpl.MAX_INFO_LENGTH), doc.getInfo());
    }

    @Test
    void toDocumentRedactsEmailAndPhoneFromInfoWhenEnabled() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john_doe");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setInfo("Contact me at john.doe@example.com or +380 (67) 123-45-67");

        ProfileSearchDocument doc = mapper.toDocument(profile);

        assertEquals("Contact me at or", doc.getInfo());
    }

    @Test
    void toDocumentDoesNotIndexInfoWhenDisabled() {
        ProfileSearchMapperImpl mapperWithoutInfo = new ProfileSearchMapperImpl(false, true);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john_doe");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setInfo("About me");

        ProfileSearchDocument doc = mapperWithoutInfo.toDocument(profile);

        assertEquals("", doc.getInfo());
    }
}
