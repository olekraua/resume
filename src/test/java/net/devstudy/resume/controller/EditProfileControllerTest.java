package net.devstudy.resume.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
        form.setItems(List.of(empty, valid));

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
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        WebDataBinder binder = new WebDataBinder(new Object());
        controller.initBinder(binder);

        assertEquals(LanguageType.SPOKEN, binder.convertIfNecessary(" SPOKEN ", LanguageType.class));
        assertEquals(null, binder.convertIfNecessary("   ", LanguageType.class));
    }

    @Test
    void initBinderConvertsLanguageLevelAndBlank() {
        EditProfileController controller = new EditProfileController(
                mock(StaticDataService.class),
                mock(CertificateStorageService.class),
                mock(PhotoStorageService.class),
                mock(PasswordEncoder.class),
                mock(Validator.class),
                mock(ProfileService.class),
                mock(CurrentProfileProvider.class));

        WebDataBinder binder = new WebDataBinder(new Object());
        controller.initBinder(binder);

        assertEquals(LanguageLevel.INTERMEDIATE, binder.convertIfNecessary(" INTERMEDIATE ", LanguageLevel.class));
        assertEquals(null, binder.convertIfNecessary("   ", LanguageLevel.class));
    }
}
