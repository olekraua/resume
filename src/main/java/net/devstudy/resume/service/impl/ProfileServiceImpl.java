package net.devstudy.resume.service.impl;

import java.util.Optional;

import net.devstudy.resume.entity.Certificate;
import net.devstudy.resume.entity.Course;
import net.devstudy.resume.entity.Education;
import net.devstudy.resume.entity.Hobby;
import net.devstudy.resume.entity.Language;
import net.devstudy.resume.entity.Practic;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.Skill;
import net.devstudy.resume.form.ContactsForm;
import net.devstudy.resume.form.InfoForm;
import net.devstudy.resume.repository.storage.CertificateRepository;
import net.devstudy.resume.repository.storage.CourseRepository;
import net.devstudy.resume.repository.storage.EducationRepository;
import net.devstudy.resume.repository.storage.HobbyRepository;
import net.devstudy.resume.repository.storage.LanguageRepository;
import net.devstudy.resume.repository.storage.PracticRepository;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.repository.storage.SkillRepository;
import net.devstudy.resume.service.ProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final PracticRepository practicRepository;
    private final EducationRepository educationRepository;
    private final CourseRepository courseRepository;
    private final LanguageRepository languageRepository;
    private final HobbyRepository hobbyRepository;
    private final CertificateRepository certificateRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<Profile> findByUid(String uid) {
        return profileRepository.findByUid(uid);
    }

    @Override
    public Optional<Profile> findWithAllByUid(String uid) {
        Optional<Profile> opt = profileRepository.findByUid(uid);
        opt.ifPresent(this::initializeCollections);
        return opt;
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Override
    public Iterable<Profile> findAllForIndexing() {
        // тимчасово: просто повертаємо все з БД
        return profileRepository.findAll();
    }

    @Override
    public Page<Profile> search(String query, Pageable pageable) {
        return profileRepository.search(query, pageable);
    }

    @Override
    @Transactional
    public void updatePassword(Long profileId, String rawPassword) {
        Profile profile = getProfileOrThrow(profileId);
        profile.setPassword(passwordEncoder.encode(rawPassword));
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public Profile register(String uid, String firstName, String lastName, String rawPassword) {
        if (profileRepository.findByUid(uid).isPresent()) {
            throw new IllegalArgumentException("Uid already exists: " + uid);
        }
        Profile profile = new Profile();
        profile.setUid(uid);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPassword(passwordEncoder.encode(rawPassword));
        profile.setCompleted(true);
        return profileRepository.save(profile);
    }

    @Override
    public Optional<Profile> findById(Long id) {
        return profileRepository.findById(id);
    }

    @Override
    @Transactional
    public void updateUid(Long profileId, String newUid) {
        Profile profile = getProfileOrThrow(profileId);
        if (profileRepository.findByUid(newUid).isPresent()) {
            throw new IllegalArgumentException("Uid already exists: " + newUid);
        }
        profile.setUid(newUid);
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public void updateSkills(Long profileId, java.util.List<Skill> items) {
        Profile profile = getProfileOrThrow(profileId);
        skillRepository.deleteByProfileId(profileId);
        if (items != null) {
            for (Skill skill : items) {
                skill.setId(null);
                skill.setProfile(profile);
            }
            skillRepository.saveAll(items);
        }
    }

    @Override
    @Transactional
    public void updatePractics(Long profileId, java.util.List<Practic> items) {
        Profile profile = getProfileOrThrow(profileId);
        practicRepository.deleteByProfileId(profileId);
        if (items != null) {
            for (Practic item : items) {
                item.setId(null);
                item.setProfile(profile);
            }
            practicRepository.saveAll(items);
        }
    }

    @Override
    @Transactional
    public void updateEducations(Long profileId, java.util.List<Education> items) {
        Profile profile = getProfileOrThrow(profileId);
        educationRepository.deleteByProfileId(profileId);
        if (items != null) {
            for (Education item : items) {
                item.setId(null);
                item.setProfile(profile);
            }
            educationRepository.saveAll(items);
        }
    }

    @Override
    @Transactional
    public void updateCourses(Long profileId, java.util.List<Course> items) {
        Profile profile = getProfileOrThrow(profileId);
        courseRepository.deleteByProfileId(profileId);
        if (items != null) {
            for (Course item : items) {
                item.setId(null);
                item.setProfile(profile);
            }
            courseRepository.saveAll(items);
        }
    }

    @Override
    @Transactional
    public void updateLanguages(Long profileId, java.util.List<Language> items) {
        Profile profile = getProfileOrThrow(profileId);
        languageRepository.deleteByProfileId(profileId);
        if (items != null) {
            for (Language item : items) {
                item.setId(null);
                item.setProfile(profile);
            }
            languageRepository.saveAll(items);
        }
    }

    @Override
    @Transactional
    public void updateHobbies(Long profileId, java.util.List<Long> hobbyIds) {
        Profile profile = getProfileOrThrow(profileId);
        hobbyRepository.deleteByProfileId(profileId);
        if (hobbyIds != null && !hobbyIds.isEmpty()) {
            // беремо існуючі хобі за id, щоб зберегти назву; створюємо нові записи для профілю
            Iterable<Hobby> found = hobbyRepository.findAllById(hobbyIds);
            java.util.List<Hobby> toSave = new java.util.ArrayList<>();
            for (Hobby hobby : found) {
                Hobby clone = new Hobby();
                clone.setName(hobby.getName());
                clone.setProfile(profile);
                toSave.add(clone);
            }
            hobbyRepository.saveAll(toSave);
        }
    }

    @Override
    @Transactional
    public void updateContacts(Long profileId, ContactsForm form) {
        Profile profile = getProfileOrThrow(profileId);
        profile.setPhone(form.getPhone());
        profile.setEmail(form.getEmail());
        profile.getContacts().setFacebook(form.getFacebook());
        profile.getContacts().setLinkedin(form.getLinkedin());
        profile.getContacts().setGithub(form.getGithub());
        profile.getContacts().setStackoverflow(form.getStackoverflow());
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public void updateInfo(Long profileId, InfoForm form) {
        Profile profile = getProfileOrThrow(profileId);
        profile.setBirthDay(form.getBirthDay());
        profile.setCountry(form.getCountry());
        profile.setCity(form.getCity());
        profile.setObjective(form.getObjective());
        profile.setSummary(form.getSummary());
        profile.setInfo(form.getInfo());
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public void updateCertificates(Long profileId, java.util.List<Certificate> items) {
        Profile profile = getProfileOrThrow(profileId);
        certificateRepository.deleteByProfileId(profileId);
        if (items != null) {
            for (Certificate item : items) {
                item.setId(null);
                item.setProfile(profile);
            }
            certificateRepository.saveAll(items);
        }
    }

    @Override
    @Transactional
    public void updatePhoto(Long profileId, String largeUrl, String smallUrl) {
        Profile profile = getProfileOrThrow(profileId);
        profile.setLargePhoto(largeUrl);
        profile.setSmallPhoto(smallUrl);
        profileRepository.save(profile);
    }

    private Profile getProfileOrThrow(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + profileId));
    }

    private void initializeCollections(Profile profile) {
        // force lazy collections to load via separate queries to avoid MultipleBagFetchException
        if (profile.getLanguages() != null) { profile.getLanguages().size(); }
        if (profile.getHobbies() != null) { profile.getHobbies().size(); }
        if (profile.getSkills() != null) { profile.getSkills().size(); }
        if (profile.getPractics() != null) { profile.getPractics().size(); }
        if (profile.getCertificates() != null) { profile.getCertificates().size(); }
        if (profile.getCourses() != null) { profile.getCourses().size(); }
        if (profile.getEducations() != null) { profile.getEducations().size(); }
    }
}
