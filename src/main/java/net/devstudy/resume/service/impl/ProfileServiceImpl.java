package net.devstudy.resume.service.impl;

import java.util.Locale;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.devstudy.resume.component.CertificateFileStorage;
import net.devstudy.resume.component.impl.UploadCertificateLinkTempStorage;
import net.devstudy.resume.entity.Certificate;
import net.devstudy.resume.entity.Contacts;
import net.devstudy.resume.entity.Course;
import net.devstudy.resume.entity.Education;
import net.devstudy.resume.entity.Hobby;
import net.devstudy.resume.entity.Language;
import net.devstudy.resume.entity.Practic;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.Skill;
import net.devstudy.resume.event.ProfileIndexingRequestedEvent;
import net.devstudy.resume.exception.UidAlreadyExistsException;
import net.devstudy.resume.form.ContactsForm;
import net.devstudy.resume.form.InfoForm;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.repository.storage.CertificateRepository;
import net.devstudy.resume.repository.storage.CourseRepository;
import net.devstudy.resume.repository.storage.EducationRepository;
import net.devstudy.resume.repository.storage.HobbyRepository;
import net.devstudy.resume.repository.storage.LanguageRepository;
import net.devstudy.resume.repository.storage.PracticRepository;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.repository.storage.SkillRepository;
import net.devstudy.resume.security.CurrentProfileProvider;
import net.devstudy.resume.service.ProfileSearchService;
import net.devstudy.resume.service.ProfileService;

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
    private final CertificateFileStorage certificateFileStorage;
    private final UploadCertificateLinkTempStorage uploadCertificateLinkTempStorage;
    private final PasswordEncoder passwordEncoder;
    private final ProfileSearchService profileSearchService;
    private final CurrentProfileProvider currentProfileProvider;
    private final ApplicationEventPublisher eventPublisher;

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
    public Optional<Profile> findByIdWithAll(Long id) {
        Optional<Profile> opt = profileRepository.findById(id);
        opt.ifPresent(this::initializeCollections);
        return opt;
    }

    @Override
    public Optional<Profile> loadCurrentProfileWithHobbies(String uid) {
        CurrentProfile current = currentProfileProvider.getCurrentProfile();
        if (current == null || !current.getUsername().equals(uid)) {
            return Optional.empty();
        }
        return findByIdWithAll(current.getId()).map(profile -> {
            if (profile.getHobbies() != null) {
                profile.getHobbies().size();
            }
            return profile;
        });
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Override
    public Iterable<Profile> findAllForIndexing() {
        return profileRepository.findAll(Pageable.unpaged()).getContent();
    }

    @Override
    public Page<Profile> search(String query, Pageable pageable) {
        try {
            return profileSearchService.search(query, pageable);
        } catch (Exception ex) {
            // fallback на JPA, якщо ES недоступний
            return profileRepository.search(query, pageable);
        }
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
        String normalizedUid = normalizeUid(uid);
        if (profileRepository.findByUid(normalizedUid).isPresent()) {
            throw new UidAlreadyExistsException(normalizedUid);
        }
        Profile profile = new Profile();
        profile.setUid(normalizedUid);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPassword(passwordEncoder.encode(rawPassword));
        profile.setCompleted(false);
        if (profile.getContacts() == null) {
            profile.setContacts(new Contacts());
        }
        Profile saved = profileRepository.save(profile);
        requestIndexing(saved.getId());
        return saved;
    }

    private boolean isProfileCompleted(Profile profile) {
        return profile.getBirthDay() != null
                && nonBlank(profile.getFirstName(), profile.getLastName(), profile.getUid(), profile.getEmail(),
                        profile.getPhone(), profile.getCountry(), profile.getCity(), profile.getObjective(),
                        profile.getSummary(), profile.getSmallPhoto());
    }

    private boolean nonBlank(String... values) {
        if (values == null) {
            return false;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String normalizeUid(String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Uid is required");
        }
        String candidate = uid.trim();
        if (!candidate.matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException("Uid must contain only latin letters, digits, '-' or '_'");
        }
        String normalized = candidate.toLowerCase(Locale.ENGLISH);
        if (normalized.length() < 3 || normalized.length() > 64) {
            throw new IllegalArgumentException("Uid must be 3-64 chars (a-z, 0-9, '-', '_')");
        }
        return normalized;
    }

    @Override
    public Optional<Profile> findById(Long id) {
        return profileRepository.findById(id);
    }

    @Override
    @Transactional
    public void updateUid(Long profileId, String newUid) {
        Profile profile = getProfileOrThrow(profileId);
        String normalizedUid = normalizeUid(newUid);
        if (profileRepository.findByUid(normalizedUid).isPresent()) {
            throw new UidAlreadyExistsException(normalizedUid);
        }
        profile.setUid(normalizedUid);
        profileRepository.save(profile);
        requestIndexing(profileId);
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
        requestIndexing(profileId);
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
        requestIndexing(profileId);
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
        requestIndexing(profileId);
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
        requestIndexing(profileId);
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
        requestIndexing(profileId);
    }

    @Override
    @Transactional
    public void updateHobbies(Long profileId, java.util.List<Long> hobbyIds) {
        Profile profile = getProfileOrThrow(profileId);
        hobbyRepository.deleteByProfileId(profileId);
        if (hobbyIds != null && !hobbyIds.isEmpty()) {
            // беремо існуючі хобі за id, щоб зберегти назву; створюємо нові записи для
            // профілю
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
        profile.setCompleted(isProfileCompleted(profile));
        requestIndexing(profileId);
    }

    @Override
    @Transactional
    public void updateContacts(Long profileId, ContactsForm form) {
        Profile profile = getProfileOrThrow(profileId);
        profile.setPhone(form.getPhone());
        profile.setEmail(form.getEmail());
        if (profile.getContacts() == null) {
            profile.setContacts(new Contacts());
        }
        profile.getContacts().setFacebook(form.getFacebook());
        profile.getContacts().setLinkedin(form.getLinkedin());
        profile.getContacts().setGithub(form.getGithub());
        profile.getContacts().setStackoverflow(form.getStackoverflow());
        profile.setCompleted(isProfileCompleted(profile));
        profileRepository.save(profile);
        requestIndexing(profileId);
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
        // оновлюємо completion-статус, якщо всі ключові поля заповнені
        profile.setCompleted(isProfileCompleted(profile));
        profileRepository.save(profile);
        requestIndexing(profileId);
    }

    @Override
    @Transactional
    public void updateCertificates(Long profileId, java.util.List<Certificate> items) {
        java.util.List<String> oldUrls = collectCertificateUrls(certificateRepository.findByProfileId(profileId));
        java.util.Set<String> newUrls = collectCertificateUrlsAsSet(items);
        Profile profile = getProfileOrThrow(profileId);
        certificateRepository.deleteByProfileId(profileId);
        if (items != null) {
            for (Certificate item : items) {
                item.setId(null);
                item.setProfile(profile);
            }
            certificateRepository.saveAll(items);
        }
        registerCertificateCleanup(oldUrls, newUrls);
        profile.setCompleted(isProfileCompleted(profile));
        requestIndexing(profileId);
    }

    @Override
    @Transactional
    public void updatePhoto(Long profileId, String largeUrl, String smallUrl) {
        Profile profile = getProfileOrThrow(profileId);
        profile.setLargePhoto(largeUrl);
        profile.setSmallPhoto(smallUrl);
        profile.setCompleted(isProfileCompleted(profile));
        profileRepository.save(profile);
        requestIndexing(profileId);
    }

    private void requestIndexing(Long profileId) {
        if (profileId == null) {
            return;
        }
        eventPublisher.publishEvent(new ProfileIndexingRequestedEvent(profileId));
    }

    private Profile getProfileOrThrow(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + profileId));
    }

    private java.util.List<String> collectCertificateUrls(java.util.List<Certificate> certificates) {
        if (certificates == null || certificates.isEmpty()) {
            return java.util.List.of();
        }
        java.util.List<String> urls = new java.util.ArrayList<>();
        for (Certificate certificate : certificates) {
            if (certificate == null) {
                continue;
            }
            if (StringUtils.hasText(certificate.getLargeUrl())) {
                urls.add(certificate.getLargeUrl());
            }
            if (StringUtils.hasText(certificate.getSmallUrl())) {
                urls.add(certificate.getSmallUrl());
            }
        }
        return urls;
    }

    private java.util.Set<String> collectCertificateUrlsAsSet(java.util.List<Certificate> certificates) {
        if (certificates == null || certificates.isEmpty()) {
            return java.util.Set.of();
        }
        java.util.Set<String> urls = new java.util.HashSet<>();
        for (Certificate certificate : certificates) {
            if (certificate == null) {
                continue;
            }
            if (StringUtils.hasText(certificate.getLargeUrl())) {
                urls.add(certificate.getLargeUrl());
            }
            if (StringUtils.hasText(certificate.getSmallUrl())) {
                urls.add(certificate.getSmallUrl());
            }
        }
        return urls;
    }

    private void registerCertificateCleanup(java.util.List<String> oldUrls, java.util.Set<String> newUrls) {
        java.util.List<String> toRemove = oldUrls.stream()
                .filter(url -> !newUrls.contains(url))
                .toList();
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            uploadCertificateLinkTempStorage.clearImageLinks();
            certificateFileStorage.removeAll(toRemove);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                uploadCertificateLinkTempStorage.clearImageLinks();
                certificateFileStorage.removeAll(toRemove);
            }
        });
    }

    private void initializeCollections(Profile profile) {
        // force lazy collections to load via separate queries to avoid
        // MultipleBagFetchException
        if (profile.getLanguages() != null) {
            profile.getLanguages().size();
        }
        if (profile.getHobbies() != null) {
            profile.getHobbies().size();
        }
        if (profile.getSkills() != null) {
            profile.getSkills().size();
        }
        if (profile.getPractics() != null) {
            profile.getPractics().size();
        }
        if (profile.getCertificates() != null) {
            profile.getCertificates().size();
        }
        if (profile.getCourses() != null) {
            profile.getCourses().size();
        }
        if (profile.getEducations() != null) {
            profile.getEducations().size();
        }
    }
}
