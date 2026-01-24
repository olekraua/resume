package net.devstudy.resume.web.controller;

import static net.devstudy.resume.shared.constants.Constants.UI.MAX_PROFILES_PER_PAGE;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.api.model.Certificate;
import net.devstudy.resume.profile.api.model.Contacts;
import net.devstudy.resume.profile.api.model.Course;
import net.devstudy.resume.profile.api.model.Education;
import net.devstudy.resume.profile.api.model.Language;
import net.devstudy.resume.profile.api.model.Practic;
import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.profile.api.model.Skill;
import net.devstudy.resume.profile.api.service.ProfileService;
import net.devstudy.resume.shared.model.LanguageLevel;
import net.devstudy.resume.shared.model.LanguageType;
import net.devstudy.resume.staticdata.api.model.Hobby;
import net.devstudy.resume.web.dto.PageResponse;
import net.devstudy.resume.web.dto.ProfileSummary;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileApiController {

    private static final int MAX_PAGE_SIZE = 50;

    private final ProfileService profileService;

    @GetMapping
    public PageResponse<ProfileSummary> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", required = false) Integer size) {
        int safePage = Math.max(0, page);
        int safeSize = normalizeSize(size);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("id"));
        Page<Profile> result = profileService.findAll(pageRequest);
        List<ProfileSummary> items = mapList(result.getContent(), ProfileSummary::from);
        return new PageResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @GetMapping("/{uid}")
    public ProfileDetails profile(@PathVariable String uid) {
        Profile profile = profileService.findWithAllByUid(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        return toDetails(profile);
    }

    private ProfileDetails toDetails(Profile profile) {
        LocalDate birthDay = profile.getBirthDay() == null ? null : profile.getBirthDay().toLocalDate();
        ContactsItem contacts = toContacts(profile.getContacts());
        List<SkillItem> skills = mapList(profile.getSkills(), this::toSkill);
        List<LanguageItem> languages = mapList(profile.getLanguages(), this::toLanguage);
        List<HobbyItem> hobbies = mapList(profile.getHobbies(), this::toHobby);
        List<PracticItem> practics = mapList(profile.getPractics(), this::toPractic);
        List<CertificateItem> certificates = mapList(profile.getCertificates(), this::toCertificate);
        List<CourseItem> courses = mapList(profile.getCourses(), this::toCourse);
        List<EducationItem> educations = mapList(profile.getEducations(), this::toEducation);
        return new ProfileDetails(
                profile.getUid(),
                profile.getFirstName(),
                profile.getLastName(),
                trimToEmpty(profile.getFullName()),
                profile.getAge(),
                birthDay,
                profile.getCity(),
                profile.getCountry(),
                profile.getObjective(),
                profile.getSummary(),
                profile.getInfo(),
                profile.getLargePhoto(),
                profile.getSmallPhoto(),
                profile.getPhone(),
                profile.getEmail(),
                profile.isCompleted(),
                contacts,
                skills,
                languages,
                hobbies,
                practics,
                certificates,
                courses,
                educations
        );
    }

    private SkillItem toSkill(Skill skill) {
        return new SkillItem(skill.getCategory(), skill.getValue());
    }

    private LanguageItem toLanguage(Language language) {
        LanguageType type = language.getType();
        boolean hasType = type != null && type != LanguageType.ALL;
        return new LanguageItem(language.getName(), language.getLevel(), type, hasType);
    }

    private HobbyItem toHobby(Hobby hobby) {
        return new HobbyItem(hobby.getId(), hobby.getName(), hobby.getCssClassName());
    }

    private PracticItem toPractic(Practic practic) {
        return new PracticItem(
                practic.getCompany(),
                practic.getPosition(),
                practic.getResponsibilities(),
                practic.getBeginDate(),
                practic.getFinishDate(),
                practic.isFinish(),
                practic.getDemo(),
                practic.getSrc()
        );
    }

    private CertificateItem toCertificate(Certificate certificate) {
        return new CertificateItem(
                certificate.getName(),
                certificate.getIssuer(),
                certificate.getSmallUrl(),
                certificate.getLargeUrl()
        );
    }

    private CourseItem toCourse(Course course) {
        return new CourseItem(
                course.getName(),
                course.getSchool(),
                course.getFinishDate(),
                course.isFinish()
        );
    }

    private EducationItem toEducation(Education education) {
        return new EducationItem(
                education.getFaculty(),
                education.getSummary(),
                education.getUniversity(),
                education.getBeginYear(),
                education.getFinishYear(),
                education.isFinish()
        );
    }

    private ContactsItem toContacts(Contacts contacts) {
        if (contacts == null) {
            return new ContactsItem(null, null, null, null);
        }
        return new ContactsItem(
                contacts.getFacebook(),
                contacts.getLinkedin(),
                contacts.getGithub(),
                contacts.getStackoverflow()
        );
    }

    private int normalizeSize(Integer size) {
        int effective = size == null ? MAX_PROFILES_PER_PAGE : size;
        return Math.max(1, Math.min(effective, MAX_PAGE_SIZE));
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static <T, R> List<R> mapList(List<T> items, Function<T, R> mapper) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Objects::nonNull)
                .toList();
    }

    public record ProfileDetails(
            String uid,
            String firstName,
            String lastName,
            String fullName,
            int age,
            LocalDate birthDay,
            String city,
            String country,
            String objective,
            String summary,
            String info,
            String largePhoto,
            String smallPhoto,
            String phone,
            String email,
            boolean completed,
            ContactsItem contacts,
            List<SkillItem> skills,
            List<LanguageItem> languages,
            List<HobbyItem> hobbies,
            List<PracticItem> practics,
            List<CertificateItem> certificates,
            List<CourseItem> courses,
            List<EducationItem> educations
    ) {
    }

    public record ContactsItem(
            String facebook,
            String linkedin,
            String github,
            String stackoverflow
    ) {
    }

    public record SkillItem(
            String category,
            String value
    ) {
    }

    public record LanguageItem(
            String name,
            LanguageLevel level,
            LanguageType type,
            boolean hasLanguageType
    ) {
    }

    public record HobbyItem(
            Long id,
            String name,
            String cssClassName
    ) {
    }

    public record PracticItem(
            String company,
            String position,
            String responsibilities,
            LocalDate beginDate,
            LocalDate finishDate,
            boolean finish,
            String demo,
            String src
    ) {
    }

    public record CertificateItem(
            String name,
            String issuer,
            String smallUrl,
            String largeUrl
    ) {
    }

    public record CourseItem(
            String name,
            String school,
            LocalDate finishDate,
            boolean finish
    ) {
    }

    public record EducationItem(
            String faculty,
            String summary,
            String university,
            Integer beginYear,
            Integer finishYear,
            boolean finish
    ) {
    }
}
