package net.devstudy.resume.profile.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ProfileTest {

    @Test
    void getAgeReturnsZeroWhenBirthDayNull() {
        Profile profile = new Profile();

        assertEquals(0, profile.getAge());
    }

    @Test
    void getAgeReturnsYearsBetweenBirthAndNow() {
        Profile profile = new Profile();
        LocalDate birth = LocalDate.now().minusYears(30);
        profile.setBirthDay(Date.valueOf(birth));

        assertEquals(30, profile.getAge());
    }

    @Test
    void getProfilePhotoReturnsPlaceholderWhenLargePhotoNull() {
        Profile profile = new Profile();

        assertEquals("/static/img/profile-placeholder.png", profile.getProfilePhoto());
    }

    @Test
    void getProfilePhotoReturnsLargePhotoWhenPresent() {
        Profile profile = new Profile();
        profile.setLargePhoto("/uploads/user1-large.jpg");

        assertEquals("/uploads/user1-large.jpg", profile.getProfilePhoto());
    }

    @Test
    void updateProfilePhotosReturnsOldLargePhotoAndUpdatesFields() {
        Profile profile = new Profile();
        profile.setLargePhoto("/uploads/old-large.jpg");
        profile.setSmallPhoto("/uploads/old-small.jpg");

        String oldLarge = profile.updateProfilePhotos("/uploads/new-large.jpg", "/uploads/new-small.jpg");

        assertEquals("/uploads/old-large.jpg", oldLarge);
        assertEquals("/uploads/new-large.jpg", profile.getLargePhoto());
        assertEquals("/uploads/new-small.jpg", profile.getSmallPhoto());
    }

    @Test
    void getContactsReturnsExistingContacts() {
        Profile profile = new Profile();
        Contacts contacts = new Contacts();
        profile.setContacts(contacts);

        assertEquals(contacts, profile.getContacts());
    }

    @Test
    void getContactsCreatesContactsWhenNull() {
        Profile profile = new Profile();

        Contacts first = profile.getContacts();
        Contacts second = profile.getContacts();

        assertEquals(first, second);
    }
}
