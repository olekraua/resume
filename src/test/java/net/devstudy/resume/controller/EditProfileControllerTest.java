package net.devstudy.resume.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import net.devstudy.resume.entity.Contacts;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.SkillCategory;
import net.devstudy.resume.entity.Skill;
import net.devstudy.resume.entity.Practic;
import net.devstudy.resume.entity.Education;
import net.devstudy.resume.entity.Course;
import net.devstudy.resume.entity.Language;
import net.devstudy.resume.entity.Hobby;
import net.devstudy.resume.entity.Certificate;
import net.devstudy.resume.form.ContactsForm;
import net.devstudy.resume.form.CourseForm;
import net.devstudy.resume.form.EducationForm;
import net.devstudy.resume.form.InfoForm;
import net.devstudy.resume.form.ChangePasswordForm;
import net.devstudy.resume.form.ProfileMainForm;
import net.devstudy.resume.form.SkillForm;
import net.devstudy.resume.form.PracticForm;
import net.devstudy.resume.form.LanguageForm;
import net.devstudy.resume.form.HobbyForm;
import net.devstudy.resume.form.CertificateForm;
import net.devstudy.resume.model.LanguageLevel;
import net.devstudy.resume.model.LanguageType;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.security.CurrentProfileProvider;
import net.devstudy.resume.service.CertificateStorageService;
import net.devstudy.resume.service.PhotoStorageService;
import net.devstudy.resume.service.ProfileService;
import net.devstudy.resume.service.StaticDataService;

class EditProfileControllerTest {

    @Test
    void skillCategoriesUsesStaticDataService() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CertificateStorageService certificateStorageService = mock(CertificateStorageService.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Validator validator = mock(Validator.class);
        ProfileService profileService = mock(ProfileService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);

        SkillCategory category = new SkillCategory();
        category.setCategory("Java");
        List<SkillCategory> categories = List.of(category);
        when(staticDataService.findSkillCategories()).thenReturn(categories);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                certificateStorageService,
                photoStorageService,
                passwordEncoder,
                validator,
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<SkillCategory> result = controller.skillCategories();

        assertSame(categories, result);
        verify(staticDataService).findSkillCategories();
    }

    @Test
    void editRootRedirectsToProfileEdit() {
        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        String view = controller.editRoot("john-doe");

        assertEquals("redirect:/john-doe/edit/profile", view);
    }

    @Test
    void editProfileRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        String view = controller.editProfile("john-doe", new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).findByIdWithAll(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void editProfileThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editProfile("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void editProfileThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editProfile("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editProfilePopulatesModelAndReturnsView() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("owner-user");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setBirthDay(Date.valueOf("2000-01-02"));
        profile.setCountry("UA");
        profile.setCity("Kyiv");
        profile.setEmail("john@example.com");
        profile.setPhone("+380000000000");
        profile.setObjective("Objective");
        profile.setSummary("Summary");
        profile.setInfo("Info");

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editProfile("owner-user", model);

        assertEquals("edit/profile", view);
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof ProfileMainForm);
        ProfileMainForm profileForm = (ProfileMainForm) form;
        assertEquals(profile.getBirthDay(), profileForm.getBirthDay());
        assertEquals(profile.getCountry(), profileForm.getCountry());
        assertEquals(profile.getCity(), profileForm.getCity());
        assertEquals(profile.getEmail(), profileForm.getEmail());
        assertEquals(profile.getPhone(), profileForm.getPhone());
        assertEquals(profile.getObjective(), profileForm.getObjective());
        assertEquals(profile.getSummary(), profileForm.getSummary());
        assertEquals(profile.getInfo(), profileForm.getInfo());
        verify(profileService).findByIdWithAll(1L);
    }

    private CurrentProfile currentProfile(String uid, Long id) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setUid(uid);
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        return new CurrentProfile(profile);
    }

    @Test
    void formFromProfilePopulatesSupportedForms() {
        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        Profile profile = new Profile();
        List<Skill> skills = List.of(new Skill());
        List<Practic> practics = List.of(new Practic());
        List<Education> educations = List.of(new Education());
        List<Course> courses = List.of(new Course());
        List<Language> languages = List.of(new Language());
        profile.setSkills(skills);
        profile.setPractics(practics);
        profile.setEducations(educations);
        profile.setCourses(courses);
        profile.setLanguages(languages);

        SkillForm skillForm = new SkillForm();
        assertSame(skillForm, invokeFormFromProfile(controller, skillForm, profile));
        assertSame(skills, skillForm.getItems());

        PracticForm practicForm = new PracticForm();
        assertSame(practicForm, invokeFormFromProfile(controller, practicForm, profile));
        assertSame(practics, practicForm.getItems());

        EducationForm educationForm = new EducationForm();
        assertSame(educationForm, invokeFormFromProfile(controller, educationForm, profile));
        assertSame(educations, educationForm.getItems());

        CourseForm courseForm = new CourseForm();
        assertSame(courseForm, invokeFormFromProfile(controller, courseForm, profile));
        assertSame(courses, courseForm.getItems());

        LanguageForm languageForm = new LanguageForm();
        assertSame(languageForm, invokeFormFromProfile(controller, languageForm, profile));
        assertSame(languages, languageForm.getItems());

        Profile profileWithHobbies = new Profile();
        Hobby hobby = new Hobby("Chess");
        hobby.setId(10L);
        profileWithHobbies.setHobbies(List.of(hobby));
        HobbyForm hobbyForm = new HobbyForm();
        assertSame(hobbyForm, invokeFormFromProfile(controller, hobbyForm, profileWithHobbies));
        assertEquals(List.of(10L), hobbyForm.getHobbyIds());

        Profile profileWithoutHobbies = new Profile();
        HobbyForm hobbyFormEmpty = new HobbyForm();
        assertSame(hobbyFormEmpty, invokeFormFromProfile(controller, hobbyFormEmpty, profileWithoutHobbies));
        assertEquals(List.of(), hobbyFormEmpty.getHobbyIds());

        Profile contactsProfile = new Profile();
        contactsProfile.setPhone("+380000000000");
        contactsProfile.setEmail("john@example.com");
        Contacts contacts = new Contacts();
        contacts.setFacebook("fb");
        contacts.setLinkedin("li");
        contacts.setGithub("gh");
        contacts.setStackoverflow("so");
        contactsProfile.setContacts(contacts);
        ContactsForm contactsForm = new ContactsForm();
        assertSame(contactsForm, invokeFormFromProfile(controller, contactsForm, contactsProfile));
        assertEquals(contactsProfile.getPhone(), contactsForm.getPhone());
        assertEquals(contactsProfile.getEmail(), contactsForm.getEmail());
        assertEquals("fb", contactsForm.getFacebook());
        assertEquals("li", contactsForm.getLinkedin());
        assertEquals("gh", contactsForm.getGithub());
        assertEquals("so", contactsForm.getStackoverflow());

        Profile contactsNullProfile = new Profile();
        contactsNullProfile.setPhone("123");
        contactsNullProfile.setEmail("mail@example.com");
        ContactsForm contactsFormEmpty = new ContactsForm();
        assertSame(contactsFormEmpty, invokeFormFromProfile(controller, contactsFormEmpty, contactsNullProfile));
        assertNotNull(contactsNullProfile.getContacts());

        Profile infoProfile = new Profile();
        infoProfile.setBirthDay(Date.valueOf("2002-03-04"));
        infoProfile.setCountry("Ukraine");
        infoProfile.setCity("Kyiv");
        infoProfile.setObjective("Objective");
        infoProfile.setSummary("Summary");
        infoProfile.setInfo("Info");
        InfoForm infoForm = new InfoForm();
        assertSame(infoForm, invokeFormFromProfile(controller, infoForm, infoProfile));
        assertEquals(infoProfile.getBirthDay(), infoForm.getBirthDay());
        assertEquals("Ukraine", infoForm.getCountry());
        assertEquals("Kyiv", infoForm.getCity());
        assertEquals("Objective", infoForm.getObjective());
        assertEquals("Summary", infoForm.getSummary());
        assertEquals("Info", infoForm.getInfo());

        Object plain = new Object();
        assertSame(plain, invokeFormFromProfile(controller, plain, infoProfile));
    }

    private Object invokeFormFromProfile(EditProfileController controller, Object form, Profile profile) {
        try {
            Method method = EditProfileController.class.getDeclaredMethod("formFromProfile", Object.class, Profile.class);
            method.setAccessible(true);
            return method.invoke(controller, form, profile);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Failed to invoke formFromProfile", ex);
        }
    }

    @Test
    void saveProfileRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        String view = controller.saveProfile("john-doe", new ProfileMainForm(), bindingResult,
                new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveProfileReturnsFormWhenBindingErrors() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        ProfileMainForm form = new ProfileMainForm();
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveProfile("john-doe", form, bindingResult, model);

        assertEquals("edit/profile", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(profileService, never()).updateInfo(anyLong(), any(InfoForm.class));
        verify(profileService, never()).updateContacts(anyLong(), any(ContactsForm.class));
    }

    @Test
    void saveProfileThrowsAccessDeniedForOtherUser() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveProfile("other-user", new ProfileMainForm(), bindingResult,
                        new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveProfileThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveProfile("owner-user", new ProfileMainForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveProfileSkipsEmptyPhotoAndUpdatesInfoAndContacts() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setContacts(new Contacts());

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false, false);

        ProfileMainForm form = new ProfileMainForm();
        form.setBirthDay(Date.valueOf("2001-02-03"));
        form.setCountry("UA");
        form.setCity("Kyiv");
        form.setEmail("john@example.com");
        form.setPhone("+380000000000");
        form.setObjective("Objective");
        form.setSummary("Summary");
        form.setInfo("Info");

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveProfile("john-doe", form, bindingResult, model);

        assertEquals("redirect:/john-doe/edit/profile?success", view);
        verify(photoStorageService, never()).store(any(MultipartFile.class));
        ArgumentCaptor<InfoForm> infoCaptor = ArgumentCaptor.forClass(InfoForm.class);
        ArgumentCaptor<ContactsForm> contactsCaptor = ArgumentCaptor.forClass(ContactsForm.class);
        verify(profileService).updateInfo(anyLong(), infoCaptor.capture());
        verify(profileService).updateContacts(anyLong(), contactsCaptor.capture());
        InfoForm infoForm = infoCaptor.getValue();
        ContactsForm contactsForm = contactsCaptor.getValue();
        assertEquals(form.getBirthDay(), infoForm.getBirthDay());
        assertEquals(form.getCountry(), infoForm.getCountry());
        assertEquals(form.getCity(), infoForm.getCity());
        assertEquals(form.getObjective(), infoForm.getObjective());
        assertEquals(form.getSummary(), infoForm.getSummary());
        assertEquals(form.getInfo(), infoForm.getInfo());
        assertEquals(form.getEmail(), contactsForm.getEmail());
        assertEquals(form.getPhone(), contactsForm.getPhone());
    }

    @Test
    void saveProfileUpdatesPhotoWhenProvided() throws Exception {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setContacts(new Contacts());

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false, false);

        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);
        when(photoStorageService.store(photo)).thenReturn(new String[] {"large.jpg", "small.jpg"});

        ProfileMainForm form = new ProfileMainForm();
        form.setProfilePhoto(photo);

        String view = controller.saveProfile("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/profile?success", view);
        verify(profileService).updatePhoto(1L, "large.jpg", "small.jpg");
        assertEquals("large.jpg", profile.getLargePhoto());
        assertEquals("small.jpg", profile.getSmallPhoto());
        verify(profileService).updateInfo(anyLong(), any(InfoForm.class));
        verify(profileService).updateContacts(anyLong(), any(ContactsForm.class));
    }

    @Test
    void saveProfileReturnsFormWhenPhotoStoreFails() throws Exception {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false, true);

        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);
        when(photoStorageService.store(photo)).thenThrow(new RuntimeException("boom"));

        ProfileMainForm form = new ProfileMainForm();
        form.setProfilePhoto(photo);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.saveProfile("john-doe", form, bindingResult, model);

        assertEquals("edit/profile", view);
        verify(bindingResult).rejectValue("profilePhoto", "photo.invalid", "boom");
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(profileService, never()).updateInfo(anyLong(), any(InfoForm.class));
        verify(profileService, never()).updateContacts(anyLong(), any(ContactsForm.class));
    }

    @Test
    void editSkillsRedirectsToLoginWhenCurrentProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<SkillCategory> categories = List.of(new SkillCategory());
        when(staticDataService.findSkillCategories()).thenReturn(categories);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editSkills("john-doe", model);

        assertEquals("redirect:/login", view);
        assertSame(categories, model.get("skillCategories"));
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editSkillsThrowsAccessDeniedForDifferentUid() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findSkillCategories()).thenReturn(List.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editSkills("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editSkillsThrowsNotFoundWhenProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findSkillCategories()).thenReturn(List.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editSkills("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editSkillsPopulatesModelAndReturnsView() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        SkillCategory category = new SkillCategory();
        category.setCategory("Java");
        List<SkillCategory> categories = List.of(category);
        when(staticDataService.findSkillCategories()).thenReturn(categories);

        Skill skill = new Skill();
        skill.setCategory("Backend");
        skill.setValue("Spring");
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setSkills(List.of(skill));

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editSkills("john-doe", model);

        assertEquals("edit/skills", view);
        assertSame(categories, model.get("skillCategories"));
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof SkillForm);
        SkillForm skillForm = (SkillForm) form;
        assertSame(profile.getSkills(), skillForm.getItems());
    }

    @Test
    void saveSkillsRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        SkillForm form = new SkillForm();
        String view = controller.saveSkills("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updateSkills(anyLong(), any());
    }

    @Test
    void saveSkillsThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveSkills("other-user", new SkillForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveSkillsThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveSkills("john-doe", new SkillForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveSkillsReturnsFormWhenBindingErrors() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        SkillForm form = new SkillForm();
        form.setItems(List.of(new Skill()));
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveSkills("john-doe", form, bindingResult, model);

        assertEquals("edit/skills", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(profileService, never()).updateSkills(anyLong(), any());
    }

    @Test
    void saveSkillsUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);

        Skill skill = new Skill();
        skill.setCategory("Backend");
        skill.setValue("Spring");
        List<Skill> items = List.of(skill);
        SkillForm form = new SkillForm();
        form.setItems(items);

        String view = controller.saveSkills("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/skills?success", view);
        verify(profileService).updateSkills(1L, items);
    }

    @Test
    void savePracticsRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        PracticForm form = new PracticForm();
        String view = controller.savePractics("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updatePractics(anyLong(), any());
    }

    @Test
    void savePracticsThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.savePractics("other-user", new PracticForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void savePracticsThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.savePractics("john-doe", new PracticForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void savePracticsReturnsFormWhenItemsEmpty() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findPracticsYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        PracticForm form = new PracticForm();
        form.setItems(null);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.savePractics("john-doe", form, bindingResult, model);

        assertEquals("edit/practics", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        verify(bindingResult).reject("practics.empty", "Додайте хоча б одну практику");
        verify(profileService, never()).updatePractics(anyLong(), any());
    }

    @Test
    void savePracticsReturnsFormWhenViolationsFound() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Validator validator = mock(Validator.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                validator,
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findPracticsYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        Practic practic = new Practic();
        practic.setCompany("Acme");
        PracticForm form = new PracticForm();
        form.setItems(List.of(practic));

        @SuppressWarnings("unchecked")
        ConstraintViolation<Practic> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("company");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Company required");
        when(validator.validate(any(Practic.class))).thenReturn(Set.of(violation));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.savePractics("john-doe", form, bindingResult, model);

        assertEquals("edit/practics", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));

        ArgumentCaptor<FieldError> fieldErrorCaptor = ArgumentCaptor.forClass(FieldError.class);
        verify(bindingResult).addError(fieldErrorCaptor.capture());
        FieldError fieldError = fieldErrorCaptor.getValue();
        assertEquals("items[0].company", fieldError.getField());
        assertEquals("Company required", fieldError.getDefaultMessage());
        verify(profileService, never()).updatePractics(anyLong(), any());
    }

    @Test
    void savePracticsUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Validator validator = mock(Validator.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                validator,
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(validator.validate(any(Practic.class))).thenReturn(Set.of());

        Practic empty = new Practic();
        Practic valid = new Practic();
        valid.setCompany("Acme");
        PracticForm form = new PracticForm();
        List<Practic> items = new java.util.ArrayList<>();
        items.add(null);
        items.add(empty);
        items.add(valid);
        form.setItems(items);

        String view = controller.savePractics("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/practics?success", view);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Practic>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(profileService).updatePractics(eq(1L), itemsCaptor.capture());
        List<Practic> updated = itemsCaptor.getValue();
        assertEquals(1, updated.size());
        assertSame(valid, updated.get(0));
    }

    @Test
    void saveEducationRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        EducationForm form = new EducationForm();
        String view = controller.saveEducation("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updateEducations(anyLong(), any());
    }

    @Test
    void saveEducationThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveEducation("other-user", new EducationForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveEducationThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveEducation("john-doe", new EducationForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveEducationReturnsFormWhenItemsEmpty() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Validator validator = mock(Validator.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                validator,
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findEducationYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        EducationForm form = new EducationForm();
        form.setItems(null);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveEducation("john-doe", form, bindingResult, model);

        assertEquals("edit/education", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        verify(bindingResult).reject("education.empty", "Додайте хоча б одну освіту");
        verify(validator, never()).validate(any(Education.class));
        verify(profileService, never()).updateEducations(anyLong(), any());
    }

    @Test
    void saveEducationReturnsFormWhenViolationsFound() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Validator validator = mock(Validator.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                validator,
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findEducationYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        Education education = new Education();
        education.setUniversity("Uni");
        EducationForm form = new EducationForm();
        form.setItems(List.of(education));

        @SuppressWarnings("unchecked")
        ConstraintViolation<Education> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("faculty");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Faculty required");
        when(validator.validate(any(Education.class))).thenReturn(Set.of(violation));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveEducation("john-doe", form, bindingResult, model);

        assertEquals("edit/education", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));

        ArgumentCaptor<FieldError> fieldErrorCaptor = ArgumentCaptor.forClass(FieldError.class);
        verify(bindingResult).addError(fieldErrorCaptor.capture());
        FieldError fieldError = fieldErrorCaptor.getValue();
        assertEquals("items[0].faculty", fieldError.getField());
        assertEquals("Faculty required", fieldError.getDefaultMessage());
        verify(profileService, never()).updateEducations(anyLong(), any());
    }

    @Test
    void saveEducationUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Validator validator = mock(Validator.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                validator,
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(validator.validate(any(Education.class))).thenReturn(Set.of());

        Education empty = new Education();
        Education valid = new Education();
        valid.setUniversity("Uni");
        EducationForm form = new EducationForm();
        form.setItems(List.of(empty, valid));

        String view = controller.saveEducation("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/education?success", view);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Education>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(profileService).updateEducations(eq(1L), itemsCaptor.capture());
        List<Education> updated = itemsCaptor.getValue();
        assertEquals(1, updated.size());
        assertSame(valid, updated.get(0));
    }

    @Test
    void saveCoursesRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        CourseForm form = new CourseForm();
        String view = controller.saveCourses("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updateCourses(anyLong(), any());
    }

    @Test
    void saveCoursesThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveCourses("other-user", new CourseForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveCoursesThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveCourses("john-doe", new CourseForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveCoursesReturnsFormWhenBindingErrors() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findCoursesYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        CourseForm form = new CourseForm();
        form.setItems(List.of(new Course()));
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveCourses("john-doe", form, bindingResult, model);

        assertEquals("edit/courses", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        verify(profileService, never()).updateCourses(anyLong(), any());
    }

    @Test
    void saveCoursesUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);

        Course course = new Course();
        course.setName("Course");
        CourseForm form = new CourseForm();
        form.setItems(List.of(course));

        String view = controller.saveCourses("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/courses?success", view);
        verify(profileService).updateCourses(1L, form.getItems());
    }

    @Test
    void saveLanguagesRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        LanguageForm form = new LanguageForm();
        String view = controller.saveLanguages("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updateLanguages(anyLong(), any());
    }

    @Test
    void saveLanguagesThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveLanguages("other-user", new LanguageForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveLanguagesThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveLanguages("john-doe", new LanguageForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveLanguagesReturnsFormWhenBindingErrors() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<LanguageType> languageTypes = List.of(LanguageType.SPOKEN);
        List<LanguageLevel> languageLevels = List.of(LanguageLevel.INTERMEDIATE);
        when(staticDataService.findAllLanguageTypes()).thenReturn(languageTypes);
        when(staticDataService.findAllLanguageLevels()).thenReturn(languageLevels);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        LanguageForm form = new LanguageForm();
        form.setItems(List.of(new Language()));
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveLanguages("john-doe", form, bindingResult, model);

        assertEquals("edit/languages", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        assertSame(languageTypes, model.get("languageTypes"));
        assertSame(languageLevels, model.get("languageLevels"));
        verify(profileService, never()).updateLanguages(anyLong(), any());
    }

    @Test
    void saveLanguagesUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);

        Language language = new Language();
        language.setType(LanguageType.SPOKEN);
        language.setLevel(LanguageLevel.INTERMEDIATE);
        language.setName("English");
        LanguageForm form = new LanguageForm();
        form.setItems(List.of(language));

        String view = controller.saveLanguages("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/languages?success", view);
        verify(profileService).updateLanguages(1L, form.getItems());
    }

    @Test
    void uploadPhotoRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        String view = controller.uploadPhoto("john-doe", null);

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updatePhoto(anyLong(), any(), any());
        verify(photoStorageService, never()).store(any(MultipartFile.class));
    }

    @Test
    void uploadPhotoThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.uploadPhoto("other-user", null));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
        verify(photoStorageService, never()).store(any(MultipartFile.class));
    }

    @Test
    void uploadPhotoThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.uploadPhoto("john-doe", null));

        assertEquals(404, ex.getStatusCode().value());
        verify(photoStorageService, never()).store(any(MultipartFile.class));
    }

    @Test
    void uploadPhotoRedirectsToErrorWhenFileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);
        MultipartFile profilePhoto = mock(MultipartFile.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(profilePhoto.isEmpty()).thenReturn(true);

        String view = controller.uploadPhoto("john-doe", profilePhoto);

        assertEquals("redirect:/john-doe/edit/photo?error", view);
        verify(photoStorageService, never()).store(any(MultipartFile.class));
        verify(profileService, never()).updatePhoto(anyLong(), any(), any());
    }

    @Test
    void uploadPhotoRedirectsToSuccessWhenStored() throws Exception {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);
        MultipartFile profilePhoto = mock(MultipartFile.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(profilePhoto.isEmpty()).thenReturn(false);
        when(photoStorageService.store(profilePhoto)).thenReturn(new String[] {"/large", "/small"});

        String view = controller.uploadPhoto("john-doe", profilePhoto);

        assertEquals("redirect:/john-doe/edit/photo?success", view);
        verify(profileService).updatePhoto(1L, "/large", "/small");
    }

    @Test
    void uploadPhotoRedirectsToErrorWhenStoreFails() throws Exception {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PhotoStorageService photoStorageService = mock(PhotoStorageService.class);
        MultipartFile profilePhoto = mock(MultipartFile.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                photoStorageService,
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(profilePhoto.isEmpty()).thenReturn(false);
        when(photoStorageService.store(profilePhoto)).thenThrow(new RuntimeException("boom"));

        String view = controller.uploadPhoto("john-doe", profilePhoto);

        assertEquals("redirect:/john-doe/edit/photo?error", view);
        verify(profileService, never()).updatePhoto(anyLong(), any(), any());
    }

    @Test
    void saveCertificatesRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        CertificateForm form = new CertificateForm();
        String view = controller.saveCertificates("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updateCertificates(anyLong(), any());
    }

    @Test
    void saveCertificatesThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveCertificates("other-user", new CertificateForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveCertificatesThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveCertificates("john-doe", new CertificateForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveCertificatesReturnsFormWhenBindingErrors() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        CertificateForm form = new CertificateForm();
        form.setItems(List.of(new Certificate()));
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveCertificates("john-doe", form, bindingResult, model);

        assertEquals("edit/certificates", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(profileService, never()).updateCertificates(anyLong(), any());
    }

    @Test
    void saveCertificatesUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);

        Certificate certificate = new Certificate();
        certificate.setName("Certificate");
        CertificateForm form = new CertificateForm();
        form.setItems(List.of(certificate));

        String view = controller.saveCertificates("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/certificates?success", view);
        verify(profileService).updateCertificates(1L, form.getItems());
    }

    @Test
    void saveHobbiesRedirectsToLoginWhenCurrentProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        HobbyForm form = new HobbyForm();
        String view = controller.saveHobbies("john-doe", form, null, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updateHobbies(anyLong(), any());
    }

    @Test
    void saveHobbiesThrowsAccessDeniedForDifferentUid() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveHobbies("other-user", new HobbyForm(), null, new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveHobbiesThrowsNotFoundWhenProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveHobbies("john-doe", new HobbyForm(), null, new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveHobbiesReturnsFormWhenHobbiesParamInvalid() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        List<Hobby> hobbies = List.of(new Hobby());
        when(staticDataService.findAllHobbiesWithSelected(eq(List.of()))).thenReturn(hobbies);

        HobbyForm form = new HobbyForm();
        form.setHobbyIds(List.of());
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveHobbies("john-doe", form, "1,abc", model);

        assertEquals("edit/hobbies", view);
        assertEquals("Невірний формат ідентифікатора хобі", model.get("hobbyError"));
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        assertSame(hobbies, model.get("hobbies"));
        verify(profileService, never()).updateHobbies(anyLong(), any());
    }

    @Test
    void saveHobbiesReturnsFormWhenIdsEmpty() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        List<Hobby> hobbies = List.of(new Hobby());
        when(staticDataService.findAllHobbiesWithSelected(eq(List.of()))).thenReturn(hobbies);

        HobbyForm form = new HobbyForm();
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveHobbies("john-doe", form, null, model);

        assertEquals("edit/hobbies", view);
        assertEquals("Оберіть хоча б одне хобі", model.get("hobbyError"));
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        assertSame(hobbies, model.get("hobbies"));
        verify(profileService, never()).updateHobbies(anyLong(), any());
    }

    @Test
    void saveHobbiesUpdatesAndRedirectsOnSuccess() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        HobbyForm form = new HobbyForm();
        form.setHobbyIds(List.of());
        String view = controller.saveHobbies("john-doe", form, "1,2,3", new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/hobbies?success", view);
        verify(profileService).updateHobbies(1L, List.of(1L, 2L, 3L));
        verify(staticDataService, never()).findAllHobbiesWithSelected(any());
    }

    @Test
    void saveContactsRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ContactsForm form = new ContactsForm();
        String view = controller.saveContacts("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updateContacts(anyLong(), any());
    }

    @Test
    void saveContactsThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveContacts("other-user", new ContactsForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveContactsThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveContacts("john-doe", new ContactsForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveContactsReturnsFormWhenBindingErrors() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        ContactsForm form = new ContactsForm();
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveContacts("john-doe", form, bindingResult, model);

        assertEquals("edit/contacts", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(profileService, never()).updateContacts(anyLong(), any());
    }

    @Test
    void saveContactsUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);

        ContactsForm form = new ContactsForm();
        form.setEmail("john@example.com");
        form.setPhone("+380000000000");
        String view = controller.saveContacts("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/contacts?success", view);
        verify(profileService).updateContacts(1L, form);
    }

    @Test
    void saveInfoRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        InfoForm form = new InfoForm();
        String view = controller.saveInfo("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updateInfo(anyLong(), any());
    }

    @Test
    void saveInfoThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.saveInfo("other-user", new InfoForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void saveInfoThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.saveInfo("john-doe", new InfoForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void saveInfoReturnsFormWhenBindingErrors() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        InfoForm form = new InfoForm();
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.saveInfo("john-doe", form, bindingResult, model);

        assertEquals("edit/info", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(profileService, never()).updateInfo(anyLong(), any());
    }

    @Test
    void saveInfoUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);

        InfoForm form = new InfoForm();
        form.setCountry("Ukraine");
        form.setCity("Kyiv");
        String view = controller.saveInfo("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/info?success", view);
        verify(profileService).updateInfo(1L, form);
    }

    @Test
    void savePasswordRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ChangePasswordForm form = new ChangePasswordForm();
        String view = controller.savePassword("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
        verify(profileService, never()).updatePassword(anyLong(), any());
    }

    @Test
    void savePasswordThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.savePassword("other-user", new ChangePasswordForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void savePasswordThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.savePassword("john-doe", new ChangePasswordForm(), bindingResult,
                        new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void savePasswordReturnsFormWhenBindingErrors() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(true);

        ChangePasswordForm form = new ChangePasswordForm();
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.savePassword("john-doe", form, bindingResult, model);

        assertEquals("edit/password", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(profileService, never()).updatePassword(anyLong(), any());
    }

    @Test
    void savePasswordReturnsFormWhenCurrentPasswordMismatch() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                passwordEncoder,
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(passwordEncoder.matches("old", "hash")).thenReturn(false);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("old");
        form.setNewPassword("newpass");
        form.setConfirmPassword("newpass");
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.savePassword("john-doe", form, bindingResult, model);

        assertEquals("edit/password", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(bindingResult).rejectValue("currentPassword", "password.mismatch", "Невірний поточний пароль");
        verify(profileService, never()).updatePassword(anyLong(), any());
    }

    @Test
    void savePasswordReturnsFormWhenConfirmPasswordMismatch() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                passwordEncoder,
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("old");
        form.setNewPassword("newpass");
        form.setConfirmPassword("otherpass");
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.savePassword("john-doe", form, bindingResult, model);

        assertEquals("edit/password", view);
        assertSame(profile, model.get("profile"));
        assertSame(form, model.get("form"));
        verify(bindingResult).rejectValue("confirmPassword", "password.confirm", "Паролі не співпадають");
        verify(profileService, never()).updatePassword(anyLong(), any());
    }

    @Test
    void savePasswordUpdatesAndRedirectsOnSuccess() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        BindingResult bindingResult = mock(BindingResult.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                passwordEncoder,
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("john-doe", 1L));
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("old");
        form.setNewPassword("newpass");
        form.setConfirmPassword("newpass");
        String view = controller.savePassword("john-doe", form, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/john-doe/edit/password?success", view);
        verify(profileService).updatePassword(1L, "newpass");
    }

    @Test
    void editPracticsRedirectsToLoginWhenCurrentProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findPracticsYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editPractics("john-doe", model);

        assertEquals("redirect:/login", view);
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editPracticsThrowsAccessDeniedForDifferentUid() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findPracticsYears()).thenReturn(List.of());
        when(staticDataService.findMonthMap()).thenReturn(Map.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editPractics("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editPracticsThrowsNotFoundWhenProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findPracticsYears()).thenReturn(List.of());
        when(staticDataService.findMonthMap()).thenReturn(Map.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editPractics("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editPracticsPopulatesModelAndReturnsView() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findPracticsYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);

        Practic practic = new Practic();
        practic.setCompany("Company");
        practic.setPosition("Engineer");
        practic.setResponsibilities("Work");
        practic.setBeginDate(LocalDate.of(2020, 1, 1));

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setPractics(List.of(practic));

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editPractics("john-doe", model);

        assertEquals("edit/practics", view);
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof PracticForm);
        PracticForm practicForm = (PracticForm) form;
        assertSame(profile.getPractics(), practicForm.getItems());
    }

    @Test
    void editEducationRedirectsToLoginWhenCurrentProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findEducationYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editEducation("john-doe", model);

        assertEquals("redirect:/login", view);
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editEducationThrowsAccessDeniedForDifferentUid() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findEducationYears()).thenReturn(List.of());
        when(staticDataService.findMonthMap()).thenReturn(Map.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editEducation("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editEducationThrowsNotFoundWhenProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findEducationYears()).thenReturn(List.of());
        when(staticDataService.findMonthMap()).thenReturn(Map.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editEducation("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editEducationPopulatesModelAndReturnsView() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findEducationYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);

        Education education = new Education();
        education.setUniversity("University");
        education.setFaculty("CS");
        education.setSummary("Summary");
        education.setBeginYear(2020);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setEducations(List.of(education));

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editEducation("john-doe", model);

        assertEquals("edit/education", view);
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof EducationForm);
        EducationForm educationForm = (EducationForm) form;
        assertSame(profile.getEducations(), educationForm.getItems());
    }

    @Test
    void editCoursesRedirectsToLoginWhenCurrentProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findCoursesYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editCourses("john-doe", model);

        assertEquals("redirect:/login", view);
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editCoursesThrowsAccessDeniedForDifferentUid() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findCoursesYears()).thenReturn(List.of());
        when(staticDataService.findMonthMap()).thenReturn(Map.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editCourses("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editCoursesThrowsNotFoundWhenProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findCoursesYears()).thenReturn(List.of());
        when(staticDataService.findMonthMap()).thenReturn(Map.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editCourses("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editCoursesPopulatesModelAndReturnsView() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Integer> years = List.of(2024, 2023);
        Map<Integer, String> months = Map.of(1, "Jan");
        when(staticDataService.findCoursesYears()).thenReturn(years);
        when(staticDataService.findMonthMap()).thenReturn(months);

        Course course = new Course();
        course.setName("Spring");
        course.setSchool("DevSchool");

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setCourses(List.of(course));

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editCourses("john-doe", model);

        assertEquals("edit/courses", view);
        assertSame(years, model.get("years"));
        assertSame(months, model.get("months"));
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof CourseForm);
        CourseForm courseForm = (CourseForm) form;
        assertSame(profile.getCourses(), courseForm.getItems());
    }

    @Test
    void editLanguagesRedirectsToLoginWhenCurrentProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<LanguageType> types = List.of(LanguageType.SPOKEN);
        List<LanguageLevel> levels = List.of(LanguageLevel.INTERMEDIATE);
        when(staticDataService.findAllLanguageTypes()).thenReturn(types);
        when(staticDataService.findAllLanguageLevels()).thenReturn(levels);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editLanguages("john-doe", model);

        assertEquals("redirect:/login", view);
        assertSame(types, model.get("languageTypes"));
        assertSame(levels, model.get("languageLevels"));
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editLanguagesThrowsAccessDeniedForDifferentUid() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findAllLanguageTypes()).thenReturn(List.of());
        when(staticDataService.findAllLanguageLevels()).thenReturn(List.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editLanguages("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editLanguagesThrowsNotFoundWhenProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findAllLanguageTypes()).thenReturn(List.of());
        when(staticDataService.findAllLanguageLevels()).thenReturn(List.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editLanguages("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editLanguagesPopulatesModelAndReturnsView() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<LanguageType> types = List.of(LanguageType.SPOKEN);
        List<LanguageLevel> levels = List.of(LanguageLevel.INTERMEDIATE);
        when(staticDataService.findAllLanguageTypes()).thenReturn(types);
        when(staticDataService.findAllLanguageLevels()).thenReturn(levels);

        Language language = new Language();
        language.setName("English");
        language.setType(LanguageType.SPOKEN);
        language.setLevel(LanguageLevel.INTERMEDIATE);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setLanguages(List.of(language));

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editLanguages("john-doe", model);

        assertEquals("edit/languages", view);
        assertSame(types, model.get("languageTypes"));
        assertSame(levels, model.get("languageLevels"));
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof LanguageForm);
        LanguageForm languageForm = (LanguageForm) form;
        assertSame(profile.getLanguages(), languageForm.getItems());
    }

    @Test
    void editHobbiesRedirectsToLoginWhenCurrentProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        List<Hobby> hobbies = List.of(new Hobby("Chess"));
        when(staticDataService.findAllHobbiesWithSelected(null)).thenReturn(hobbies);
        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);
        when(profileService.loadCurrentProfileWithHobbies("john-doe"))
                .thenReturn(Optional.of(new Profile()));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editHobbies("john-doe", model);

        assertEquals("redirect:/login", view);
        assertSame(hobbies, model.get("hobbies"));
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editHobbiesThrowsAccessDeniedForDifferentUid() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findAllHobbiesWithSelected(null)).thenReturn(List.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.loadCurrentProfileWithHobbies("other-user")).thenReturn(Optional.empty());

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editHobbies("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editHobbiesThrowsNotFoundWhenProfileMissing() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(staticDataService.findAllHobbiesWithSelected(null)).thenReturn(List.of());
        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.loadCurrentProfileWithHobbies("owner-user")).thenReturn(Optional.empty());
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editHobbies("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editHobbiesPopulatesModelAndReturnsView() {
        StaticDataService staticDataService = mock(StaticDataService.class);
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                staticDataService,
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Hobby selected = new Hobby("Chess");
        selected.setId(10L);
        Profile profileWithHobbies = new Profile();
        profileWithHobbies.setHobbies(List.of(selected));
        when(profileService.loadCurrentProfileWithHobbies("john-doe"))
                .thenReturn(Optional.of(profileWithHobbies));

        List<Hobby> hobbies = List.of(new Hobby("Chess", true));
        when(staticDataService.findAllHobbiesWithSelected(List.of(10L))).thenReturn(hobbies);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setHobbies(List.of(selected));

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editHobbies("john-doe", model);

        assertEquals("edit/hobbies", view);
        assertSame(hobbies, model.get("hobbies"));
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof HobbyForm);
        HobbyForm hobbyForm = (HobbyForm) form;
        assertEquals(List.of(10L), hobbyForm.getHobbyIds());
    }

    @Test
    void editPhotoRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editPhoto("john-doe", model);

        assertEquals("redirect:/login", view);
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editPhotoThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editPhoto("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editPhotoThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editPhoto("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editPhotoPopulatesModelAndReturnsView() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editPhoto("john-doe", model);

        assertEquals("edit/photo", view);
        assertSame(profile, model.get("profile"));
        assertTrue(model.containsAttribute("form"));
        assertEquals(null, model.get("form"));
    }

    @Test
    void editContactsRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editContacts("john-doe", model);

        assertEquals("redirect:/login", view);
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editContactsThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editContacts("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editContactsThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editContacts("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editContactsPopulatesModelAndReturnsView() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Contacts contacts = new Contacts();
        contacts.setFacebook("fb");
        contacts.setLinkedin("ln");
        contacts.setGithub("gh");
        contacts.setStackoverflow("so");

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setContacts(contacts);
        profile.setEmail("john@example.com");
        profile.setPhone("+380000000000");

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editContacts("john-doe", model);

        assertEquals("edit/contacts", view);
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof ContactsForm);
        ContactsForm contactsForm = (ContactsForm) form;
        assertEquals(profile.getEmail(), contactsForm.getEmail());
        assertEquals(profile.getPhone(), contactsForm.getPhone());
        assertEquals(contacts.getFacebook(), contactsForm.getFacebook());
        assertEquals(contacts.getLinkedin(), contactsForm.getLinkedin());
        assertEquals(contacts.getGithub(), contactsForm.getGithub());
        assertEquals(contacts.getStackoverflow(), contactsForm.getStackoverflow());
    }

    @Test
    void editInfoRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editInfo("john-doe", model);

        assertEquals("redirect:/login", view);
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editInfoThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editInfo("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editInfoThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editInfo("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editInfoPopulatesModelAndReturnsView() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setBirthDay(Date.valueOf("2002-03-04"));
        profile.setCountry("UA");
        profile.setCity("Kyiv");
        profile.setObjective("Objective");
        profile.setSummary("Summary");
        profile.setInfo("Info");

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editInfo("john-doe", model);

        assertEquals("edit/info", view);
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof InfoForm);
        InfoForm infoForm = (InfoForm) form;
        assertEquals(profile.getBirthDay(), infoForm.getBirthDay());
        assertEquals(profile.getCountry(), infoForm.getCountry());
        assertEquals(profile.getCity(), infoForm.getCity());
        assertEquals(profile.getObjective(), infoForm.getObjective());
        assertEquals(profile.getSummary(), infoForm.getSummary());
        assertEquals(profile.getInfo(), infoForm.getInfo());
    }

    @Test
    void editPasswordRedirectsToLoginWhenCurrentProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editPassword("john-doe", model);

        assertEquals("redirect:/login", view);
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editPasswordThrowsAccessDeniedForDifferentUid() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> controller.editPassword("other-user", new ExtendedModelMap()));
        assertEquals("Access denied to profile", accessDeniedException.getMessage());
        verify(profileService, never()).findByIdWithAll(anyLong());
    }

    @Test
    void editPasswordThrowsNotFoundWhenProfileMissing() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        when(currentProfileProvider.getCurrentProfile()).thenReturn(currentProfile("owner-user", 1L));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.editPassword("owner-user", new ExtendedModelMap()));

        assertEquals(404, ex.getStatusCode().value());
        verify(profileService).findByIdWithAll(1L);
    }

    @Test
    void editPasswordPopulatesModelAndReturnsView() {
        CurrentProfileProvider currentProfileProvider = mock(CurrentProfileProvider.class);
        ProfileService profileService = mock(ProfileService.class);

        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                profileService,
                currentProfileProvider);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUid("john-doe");
        profile.setPassword("hash");
        profile.setFirstName("John");
        profile.setLastName("Doe");

        when(currentProfileProvider.getCurrentProfile()).thenReturn(new CurrentProfile(profile));
        when(profileService.findByIdWithAll(1L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editPassword("john-doe", model);

        assertEquals("edit/password", view);
        assertSame(profile, model.get("profile"));
        Object form = model.get("form");
        assertTrue(form instanceof ChangePasswordForm);
    }

    @Test
    void initBinderTrimsStringsAndConvertsBlankToNull() {
        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        WebDataBinder binder = new WebDataBinder(new Object());
        controller.initBinder(binder);

        assertEquals("text", binder.convertIfNecessary("  text  ", String.class));
        assertEquals(null, binder.convertIfNecessary("   ", String.class));
    }

    @Test
    void initBinderParsesDateAndAllowsEmpty() throws Exception {
        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        WebDataBinder binder = new WebDataBinder(new Object());
        controller.initBinder(binder);

        java.util.Date parsed = binder.convertIfNecessary("2020-12-31", java.util.Date.class);
        java.util.Date expected = new SimpleDateFormat("yyyy-MM-dd").parse("2020-12-31");
        assertEquals(expected, parsed);
        assertEquals(null, binder.convertIfNecessary("", Date.class));
    }

    @Test
    void initBinderConvertsLanguageTypeAndBlank() {
        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        WebDataBinder binder = new WebDataBinder(new Object());
        controller.initBinder(binder);

        assertEquals(LanguageType.SPOKEN, binder.convertIfNecessary(" SPOKEN ", LanguageType.class));
        assertEquals(LanguageType.ALL, binder.convertIfNecessary("   ", LanguageType.class));
    }

    @Test
    void initBinderConvertsLanguageLevelAndBlank() {
        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(MessageSource.class),
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        WebDataBinder binder = new WebDataBinder(new Object());
        controller.initBinder(binder);

        assertEquals(LanguageLevel.INTERMEDIATE, binder.convertIfNecessary(" INTERMEDIATE ", LanguageLevel.class));
        assertEquals(null, binder.convertIfNecessary("   ", LanguageLevel.class));
    }
}
