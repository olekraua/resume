package net.devstudy.resume.service;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import net.devstudy.resume.domain.Certificate;
import net.devstudy.resume.domain.Contacts;
import net.devstudy.resume.domain.Course;
import net.devstudy.resume.domain.Education;
import net.devstudy.resume.domain.Hobby;
import net.devstudy.resume.domain.Language;
import net.devstudy.resume.domain.Practic;
import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.domain.Skill;
import net.devstudy.resume.domain.SkillCategory;
import net.devstudy.resume.form.InfoForm;
import net.devstudy.resume.form.PasswordForm;
import net.devstudy.resume.form.SignUpForm;
import net.devstudy.resume.model.CurrentProfile;

/**
 * EditProfileService – Spring Boot 3 / Java 21 ready.
 */
public interface EditProfileService {

    @NonNull
    Profile findProfileById(@NonNull CurrentProfile currentProfile);

    @NonNull
    Contacts findContactsById(@NonNull CurrentProfile currentProfile);

    @NonNull
    Profile createNewProfile(@NonNull SignUpForm signUpForm);

    void removeProfile(@NonNull CurrentProfile currentProfile);

    @NonNull
    Profile updateProfilePassword(@NonNull CurrentProfile currentProfile, @NonNull PasswordForm form);

    void updateProfileData(@NonNull CurrentProfile currentProfile, @NonNull Profile data,
            @NonNull MultipartFile uploadPhoto);

    void updateContacts(@NonNull CurrentProfile currentProfile, @NonNull Contacts data);

    void updateInfo(@NonNull CurrentProfile currentProfile, @NonNull InfoForm form);

    @NonNull
    List<Hobby> findHobbiesWithProfileSelected(@NonNull CurrentProfile currentProfile);

    void updateHobbies(@NonNull CurrentProfile currentProfile, @NonNull List<String> hobbies);

    @NonNull
    List<Language> findLanguages(@NonNull CurrentProfile currentProfile);

    void updateLanguages(@NonNull CurrentProfile currentProfile, @NonNull List<Language> languages);

    @NonNull
    List<Skill> findSkills(@NonNull CurrentProfile currentProfile);

    @NonNull
    List<SkillCategory> findSkillCategories();

    void updateSkills(@NonNull CurrentProfile currentProfile, @NonNull List<Skill> skills);

    @NonNull
    List<Practic> findPractics(@NonNull CurrentProfile currentProfile);

    void updatePractics(@NonNull CurrentProfile currentProfile, @NonNull List<Practic> practics);

    @NonNull
    List<Education> findEducations(@NonNull CurrentProfile currentProfile);

    void updateEducations(@NonNull CurrentProfile currentProfile, @NonNull List<Education> educations);

    @NonNull
    List<Certificate> findCertificates(@NonNull CurrentProfile currentProfile);

    void updateCertificates(@NonNull CurrentProfile currentProfile, @NonNull List<Certificate> certificates);

    @NonNull
    List<Course> findCourses(@NonNull CurrentProfile currentProfile);

    void updateCourses(@NonNull CurrentProfile currentProfile, @NonNull List<Course> courses);
}
