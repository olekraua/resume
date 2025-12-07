package net.devstudy.resume.service;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.devstudy.resume.entity.Certificate;
import net.devstudy.resume.entity.Course;
import net.devstudy.resume.entity.Education;
import net.devstudy.resume.entity.Language;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.Practic;
import net.devstudy.resume.entity.Skill;
import net.devstudy.resume.form.ContactsForm;
import net.devstudy.resume.form.InfoForm;

public interface ProfileService {
    Optional<Profile> findByUid(String uid);

    Optional<Profile> findWithAllByUid(String uid);

    Page<Profile> findAll(Pageable pageable);

    Iterable<Profile> findAllForIndexing();

    Page<Profile> search(String query, Pageable pageable);

    void updatePassword(Long profileId, String rawPassword);

    Profile register(String uid, String firstName, String lastName, String rawPassword);

    Optional<Profile> findById(Long id);

    void updateUid(Long profileId, String newUid);

    void updateSkills(Long profileId, List<Skill> items);

    void updatePractics(Long profileId, List<Practic> items);

    void updateEducations(Long profileId, List<Education> items);

    void updateCourses(Long profileId, List<Course> items);

    void updateLanguages(Long profileId, List<Language> items);

    void updateHobbies(Long profileId, List<Long> hobbyIds);

    void updateContacts(Long profileId, ContactsForm form);

    void updateInfo(Long profileId, InfoForm form);

    void updateCertificates(Long profileId, List<Certificate> items);

    void updatePhoto(Long profileId, String largeUrl, String smallUrl);
}
