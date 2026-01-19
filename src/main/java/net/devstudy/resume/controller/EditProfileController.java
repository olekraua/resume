package net.devstudy.resume.controller;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import net.devstudy.resume.entity.Certificate;
import net.devstudy.resume.entity.Hobby;
import net.devstudy.resume.entity.Education;
import net.devstudy.resume.entity.Practic;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.SkillCategory;
import net.devstudy.resume.form.ChangePasswordForm;
import net.devstudy.resume.form.CertificateForm;
import net.devstudy.resume.form.ContactsForm;
import net.devstudy.resume.form.CourseForm;
import net.devstudy.resume.form.EducationForm;
import net.devstudy.resume.form.HobbyForm;
import net.devstudy.resume.form.InfoForm;
import net.devstudy.resume.form.LanguageForm;
import net.devstudy.resume.form.PracticForm;
import net.devstudy.resume.form.ProfileMainForm;
import net.devstudy.resume.form.SkillForm;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.shared.model.LanguageLevel;
import net.devstudy.resume.shared.model.LanguageType;
import net.devstudy.resume.model.UploadCertificateResult;
import net.devstudy.resume.security.CurrentProfileProvider;
import net.devstudy.resume.service.CertificateStorageService;
import net.devstudy.resume.service.PhotoStorageService;
import net.devstudy.resume.service.ProfileService;
import net.devstudy.resume.service.StaticDataService;

@Controller
@RequestMapping("/{uid}/edit")
@RequiredArgsConstructor
public class EditProfileController {

    private final StaticDataService staticDataService;
    private final CertificateStorageService certificateStorageService;
    private final PhotoStorageService photoStorageService;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final MessageSource messageSource;
    private final ProfileService profileService;
    private final CurrentProfileProvider currentProfileProvider;

    @Value("${profile.hobbies.max:5}")
    private int maxHobbies;

    @ModelAttribute("skillCategories")
    public java.util.List<SkillCategory> skillCategories() {
        return staticDataService.findSkillCategories();
    }

    @GetMapping
    public String editRoot(@PathVariable String uid) {
        return "redirect:/" + uid + "/edit/profile";
    }

    @GetMapping("/profile")
    public String editProfile(@PathVariable String uid, Model model) {
        return prepareProfileMain(uid, model);
    }

    @PostMapping("/profile")
    public String saveProfile(@PathVariable String uid, @Valid @ModelAttribute("form") ProfileMainForm form,
            BindingResult bindingResult, Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null) {
            return "redirect:/login";
        }
        Long profileId = profile.getId();
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/profile";
        }
        MultipartFile photo = form.getProfilePhoto();
        if (photo != null && !photo.isEmpty()) {
            try {
                String[] urls = photoStorageService.store(photo);
                profileService.updatePhoto(profileId, urls[0], urls[1]);
                // оновлюємо модель, щоб одразу показати завантажене фото
                profile.setLargePhoto(urls[0]);
                profile.setSmallPhoto(urls[1]);
            } catch (Exception ex) {
                bindingResult.rejectValue("profilePhoto", "photo.invalid", ex.getMessage());
            }
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/profile";
        }
        // info
        InfoForm infoForm = new InfoForm();
        infoForm.setBirthDay(form.getBirthDay());
        infoForm.setCountry(form.getCountry());
        infoForm.setCity(form.getCity());
        infoForm.setObjective(form.getObjective());
        infoForm.setSummary(form.getSummary());
        infoForm.setInfo(form.getInfo());
        profileService.updateInfo(profileId, infoForm);
        // contacts
        ContactsForm contactsForm = new ContactsForm();
        contactsForm.setEmail(form.getEmail());
        contactsForm.setPhone(form.getPhone());
        profileService.updateContacts(profileId, contactsForm);
        return "redirect:/" + uid + "/edit/profile?success";
    }

    @GetMapping("/skills")
    public String editSkills(@PathVariable String uid, Model model) {
        return prepareSkills(uid, model);
    }

    @GetMapping("/practics")
    public String editPractics(@PathVariable String uid, Model model) {
        return preparePractics(uid, model);
    }

    @GetMapping("/education")
    public String editEducation(@PathVariable String uid, Model model) {
        return prepareEducation(uid, model);
    }

    @GetMapping("/courses")
    public String editCourses(@PathVariable String uid, Model model) {
        return prepareCourses(uid, model);
    }

    @GetMapping("/languages")
    public String editLanguages(@PathVariable String uid, Model model) {
        return prepareLanguages(uid, model);
    }

    @GetMapping("/certificates")
    public String editCertificates(@PathVariable String uid, Model model) {
        return prepareCertificates(uid, model);
    }

    @GetMapping("/hobbies")
    public String editHobbies(@PathVariable String uid, Model model) {
        return prepareHobbies(uid, model);
    }

    @GetMapping("/photo")
    public String editPhoto(@PathVariable String uid, Model model) {
        return preparePhoto(uid, model);
    }

    @GetMapping("/contacts")
    public String editContacts(@PathVariable String uid, Model model) {
        return prepareContacts(uid, model);
    }

    @GetMapping("/info")
    public String editInfo(@PathVariable String uid, Model model) {
        return prepareInfo(uid, model);
    }

    @GetMapping("/password")
    public String editPassword(@PathVariable String uid, Model model) {
        return preparePassword(uid, model);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
        binder.registerCustomEditor(java.sql.Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(StringUtils.hasText(text) ? java.sql.Date.valueOf(text.trim()) : null);
            }
        });
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(LanguageType.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null || text.isBlank() ? LanguageType.ALL : LanguageType.valueOf(text.trim()));
            }
        });
        binder.registerCustomEditor(LanguageLevel.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (!StringUtils.hasText(text)) {
                    setValue(null);
                    return;
                }
                String trimmed = text.trim();
                if (trimmed.matches("\\d+")) {
                    int index = Integer.parseInt(trimmed);
                    LanguageLevel[] values = LanguageLevel.values();
                    if (index < 0 || index >= values.length) {
                        throw new IllegalArgumentException("Unsupported language level index: " + trimmed);
                    }
                    setValue(values[index]);
                } else {
                    setValue(LanguageLevel.valueOf(trimmed.toUpperCase(Locale.ROOT)));
                }
            }
        });
    }

    @PostMapping("/skills")
    public String saveSkills(@PathVariable String uid, @Valid @ModelAttribute("form") SkillForm form,
            BindingResult bindingResult, Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/skills";
        }
        profileService.updateSkills(profileId, form.getItems());
        return "redirect:/" + uid + "/edit/skills?success";
    }

    @PostMapping("/practics")
    public String savePractics(@PathVariable String uid, @ModelAttribute("form") PracticForm form,
            BindingResult bindingResult,
            Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        List<Practic> items = form.getItems();
        if (items == null) {
            items = new ArrayList<>();
        }
        List<Practic> filtered = new ArrayList<>();
        for (Practic item : items) {
            if (!isPracticEmpty(item)) {
                filtered.add(item);
            }
        }
        form.setItems(filtered);
        if (filtered.isEmpty()) {
            bindingResult.reject("practics.empty", "Додайте хоча б одну практику");
        }
        if (!filtered.isEmpty()) {
            for (int i = 0; i < filtered.size(); i++) {
                Set<ConstraintViolation<Practic>> violations = validator
                        .validate(filtered.get(i));
                for (ConstraintViolation<Practic> violation : violations) {
                    String fieldPath = "items[" + i + "]." + violation.getPropertyPath();
                    bindingResult.addError(new FieldError("form", fieldPath, violation.getMessage()));
                }
            }
        }
        if (bindingResult.hasErrors()) {
            return showPracticsForm(profile, model, form);
        }
        profileService.updatePractics(profileId, filtered);
        return "redirect:/" + uid + "/edit/practics?success";
    }

    @PostMapping("/education")
    public String saveEducation(@PathVariable String uid, @ModelAttribute("form") EducationForm form,
            BindingResult bindingResult,
            Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        List<Education> items = form.getItems();
        if (items == null) {
            items = new ArrayList<>();
        }
        List<Education> filtered = new ArrayList<>();
        for (Education item : items) {
            if (!isEducationEmpty(item)) {
                filtered.add(item);
            }
        }
        form.setItems(filtered);
        if (filtered.isEmpty()) {
            bindingResult.reject("education.empty", "Додайте хоча б одну освіту");
        }
        if (!filtered.isEmpty()) {
            for (int i = 0; i < filtered.size(); i++) {
                Set<ConstraintViolation<Education>> violations = validator.validate(filtered.get(i));
                for (ConstraintViolation<Education> violation : violations) {
                    String fieldPath = "items[" + i + "]." + violation.getPropertyPath();
                    bindingResult.addError(new FieldError("form", fieldPath, violation.getMessage()));
                }
            }
        }
        if (bindingResult.hasErrors()) {
            return showEducationForm(profile, model, form);
        }
        profileService.updateEducations(profileId, filtered);
        return "redirect:/" + uid + "/edit/education?success";
    }

    @PostMapping("/courses")
    public String saveCourses(@PathVariable String uid, @Valid @ModelAttribute("form") CourseForm form,
            BindingResult bindingResult,
            Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            model.addAttribute("years", staticDataService.findCoursesYears());
            model.addAttribute("months", staticDataService.findMonthMap());
            return "edit/courses";
        }
        profileService.updateCourses(profileId, form.getItems());
        return "redirect:/" + uid + "/edit/courses?success";
    }

    @PostMapping("/languages")
    public String saveLanguages(@PathVariable String uid, @Valid @ModelAttribute("form") LanguageForm form,
            BindingResult bindingResult,
            Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        addDuplicateLanguageErrors(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            model.addAttribute("languageTypes", staticDataService.findAllLanguageTypes());
            model.addAttribute("languageLevels", staticDataService.findAllLanguageLevels());
            addLanguageTypeLabels(model);
            return "edit/languages";
        }
        try {
            profileService.updateLanguages(profileId, form.getItems());
        } catch (DataIntegrityViolationException ex) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(
                    "language.duplicate",
                    null,
                    "Language with the same name and type already exists.",
                    locale
            );
            bindingResult.reject("language.duplicate", message);
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            model.addAttribute("languageTypes", staticDataService.findAllLanguageTypes());
            model.addAttribute("languageLevels", staticDataService.findAllLanguageLevels());
            addLanguageTypeLabels(model);
            return "edit/languages";
        }
        return "redirect:/" + uid + "/edit/languages?success";
    }

    @PostMapping("/certificates/upload")
    @ResponseBody
    public UploadCertificateResult uploadCertificate(@PathVariable String uid,
            @RequestParam("certificateFile") MultipartFile certificateFile) {
        if (resolveProfile(uid) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизований запит");
        }
        if (certificateFile == null || certificateFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Порожній файл сертифікату");
        }
        try {
            return certificateStorageService.store(certificateFile);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не вдалося зберегти сертифікат", ex);
        }
    }

    @PostMapping("/photo")
    public String uploadPhoto(@PathVariable String uid, @RequestParam("profilePhoto") MultipartFile profilePhoto) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        if (profilePhoto == null || profilePhoto.isEmpty()) {
            return "redirect:/" + uid + "/edit/photo?error";
        }
        try {
            String[] urls = photoStorageService.store(profilePhoto);
            profileService.updatePhoto(profile.getId(), urls[0], urls[1]);
            return "redirect:/" + uid + "/edit/photo?success";
        } catch (Exception ex) {
            return "redirect:/" + uid + "/edit/photo?error";
        }
    }

    @PostMapping("/photo/remove")
    public String removePhoto(@PathVariable String uid) {
        Profile profile = resolveProfile(uid);
        if (profile == null) {
            return "redirect:/login";
        }
        profileService.removePhoto(profile.getId());
        return "redirect:/" + uid + "/edit/profile";
    }

    @PostMapping("/certificates")
    public String saveCertificates(@PathVariable String uid,
            @ModelAttribute("form") net.devstudy.resume.form.CertificateForm form,
            BindingResult bindingResult, Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        List<Certificate> items = form.getItems();
        if (items == null) {
            items = new ArrayList<>();
        }
        List<Certificate> filtered = new ArrayList<>();
        for (Certificate item : items) {
            if (!isCertificateEmpty(item)) {
                filtered.add(item);
            }
        }
        form.setItems(filtered);
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/certificates";
        }
        if (!filtered.isEmpty()) {
            for (int i = 0; i < filtered.size(); i++) {
                Set<ConstraintViolation<Certificate>> violations = validator.validate(filtered.get(i));
                for (ConstraintViolation<Certificate> violation : violations) {
                    String fieldPath = "items[" + i + "]." + violation.getPropertyPath();
                    bindingResult.addError(new FieldError("form", fieldPath, violation.getMessage()));
                }
            }
        }
        addDuplicateCertificateErrors(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/certificates";
        }
        profileService.updateCertificates(profileId, filtered);
        return "redirect:/" + uid + "/edit/certificates?success";
    }

    @PostMapping("/hobbies")
    public String saveHobbies(@PathVariable String uid, @ModelAttribute("form") HobbyForm form,
            @RequestParam(value = "hobbies", required = false) String hobbiesParam,
            Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        model.addAttribute("maxHobbies", maxHobbies);
        List<Long> ids = form.getHobbyIds();
        if ((ids == null || ids.isEmpty()) && StringUtils.hasText(hobbiesParam)) {
            try {
                ids = java.util.Arrays.stream(hobbiesParam.split(","))
                        .filter(s -> !s.isBlank())
                        .map(Long::valueOf)
                        .toList();
            } catch (NumberFormatException ex) {
                model.addAttribute("hobbyError", "Невірний формат ідентифікатора хобі");
                form.setHobbyIds(ids);
                model.addAttribute("profile", profile);
                model.addAttribute("form", form);
                model.addAttribute("hobbies",
                        staticDataService.findAllHobbiesWithSelected(ids == null ? List.of() : ids));
                return "edit/hobbies";
            }
        }
        if (ids == null || ids.isEmpty()) {
            model.addAttribute("hobbyError", "Оберіть хоча б одне хобі");
            form.setHobbyIds(ids);
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            model.addAttribute("hobbies", staticDataService.findAllHobbiesWithSelected(ids == null ? List.of() : ids));
            return "edit/hobbies";
        }
        profileService.updateHobbies(profileId, ids);
        return "redirect:/" + uid + "/edit/hobbies?success";
    }

    @PostMapping("/contacts")
    public String saveContacts(@PathVariable String uid, @Valid @ModelAttribute("form") ContactsForm form,
            BindingResult bindingResult,
            Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/contacts";
        }
        profileService.updateContacts(profileId, form);
        return "redirect:/" + uid + "/edit/contacts?success";
    }

    @PostMapping("/info")
    public String saveInfo(@PathVariable String uid, @Valid @ModelAttribute("form") InfoForm form,
            BindingResult bindingResult, Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        Long profileId = profile.getId();
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/info";
        }
        profileService.updateInfo(profileId, form);
        return "redirect:/" + uid + "/edit/info?success";
    }

    @PostMapping("/password")
    public String savePassword(@PathVariable String uid, @Valid @ModelAttribute("form") ChangePasswordForm form,
            BindingResult bindingResult,
            Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/password";
        }
        if (!passwordEncoder.matches(form.getCurrentPassword(), profile.getPassword())) {
            bindingResult.rejectValue("currentPassword", "password.mismatch", "Невірний поточний пароль");
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/password";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.confirm", "Паролі не співпадають");
            model.addAttribute("profile", profile);
            model.addAttribute("form", form);
            return "edit/password";
        }
        profileService.updatePassword(profile.getId(), form.getNewPassword());
        return "redirect:/" + uid + "/edit/password?success";
    }

    private String showPracticsForm(Profile profile, Model model, PracticForm form) {
        model.addAttribute("profile", profile);
        model.addAttribute("form", form);
        model.addAttribute("years", staticDataService.findPracticsYears());
        model.addAttribute("months", staticDataService.findMonthMap());
        return "edit/practics";
    }

    private String showEducationForm(Profile profile, Model model, EducationForm form) {
        model.addAttribute("profile", profile);
        model.addAttribute("form", form);
        model.addAttribute("years", staticDataService.findEducationYears());
        model.addAttribute("months", staticDataService.findMonthMap());
        return "edit/education";
    }

    private String prepareSkills(String uid, Model model) {
        model.addAttribute("skillCategories", staticDataService.findSkillCategories());
        return prepareProfileModel(uid, model, "edit/skills", new SkillForm());
    }

    private String preparePractics(String uid, Model model) {
        model.addAttribute("years", staticDataService.findPracticsYears());
        model.addAttribute("months", staticDataService.findMonthMap());
        return prepareProfileModel(uid, model, "edit/practics", new PracticForm());
    }

    private String prepareEducation(String uid, Model model) {
        model.addAttribute("years", staticDataService.findEducationYears());
        model.addAttribute("months", staticDataService.findMonthMap());
        return prepareProfileModel(uid, model, "edit/education", new EducationForm());
    }

    private String prepareCourses(String uid, Model model) {
        model.addAttribute("years", staticDataService.findCoursesYears());
        model.addAttribute("months", staticDataService.findMonthMap());
        return prepareProfileModel(uid, model, "edit/courses", new CourseForm());
    }

    private String prepareLanguages(String uid, Model model) {
        model.addAttribute("languageTypes", staticDataService.findAllLanguageTypes());
        model.addAttribute("languageLevels", staticDataService.findAllLanguageLevels());
        addLanguageTypeLabels(model);
        return prepareProfileModel(uid, model, "edit/languages", new LanguageForm());
    }

    private String prepareCertificates(String uid, Model model) {
        return prepareProfileModel(uid, model, "edit/certificates", new net.devstudy.resume.form.CertificateForm());
    }

    private String preparePhoto(String uid, Model model) {
        return prepareProfileModel(uid, model, "edit/photo", null);
    }

    private String prepareHobbies(String uid, Model model) {
        HobbyForm form = new HobbyForm();
        Profile profile = resolveProfile(uid);
        if (profile == null) {
            model.addAttribute("hobbies", staticDataService.findAllHobbiesWithSelected(form.getHobbyIds()));
            model.addAttribute("maxHobbies", maxHobbies);
            return "redirect:/login";
        }
        String view = prepareProfileModel(profile, model, "edit/hobbies", form);
        model.addAttribute("hobbies", staticDataService.findAllHobbiesWithSelected(form.getHobbyIds()));
        model.addAttribute("maxHobbies", maxHobbies);
        return view;
    }

    private String prepareContacts(String uid, Model model) {
        return prepareProfileModel(uid, model, "edit/contacts", new ContactsForm());
    }

    private String prepareInfo(String uid, Model model) {
        return prepareProfileModel(uid, model, "edit/info", new InfoForm());
    }

    private String preparePassword(String uid, Model model) {
        return prepareProfileModel(uid, model, "edit/password", new ChangePasswordForm());
    }

    private void addLanguageTypeLabels(Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        Map<LanguageType, String> labels = new LinkedHashMap<>();
        for (LanguageType type : LanguageType.values()) {
            String label = messageSource.getMessage("language.type." + type.name(), null, type.name(), locale);
            if (label == null || label.isBlank()) {
                label = type.name();
            }
            labels.put(type, label);
        }
        model.addAttribute("languageTypeLabels", labels);
    }

    private void addDuplicateLanguageErrors(LanguageForm form, BindingResult bindingResult) {
        if (form == null || form.getItems() == null || form.getItems().isEmpty()) {
            return;
        }
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                "language.duplicate",
                null,
                "Language with the same name and type already exists.",
                locale
        );
        Map<String, Integer> seen = new LinkedHashMap<>();
        java.util.Set<Integer> flagged = new java.util.HashSet<>();
        List<net.devstudy.resume.entity.Language> items = form.getItems();
        for (int i = 0; i < items.size(); i++) {
            net.devstudy.resume.entity.Language item = items.get(i);
            if (item == null) {
                continue;
            }
            String name = item.getName();
            String normalized = name == null ? "" : name.trim().replaceAll("\\s+", " ");
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            LanguageType type = item.getType() == null ? LanguageType.ALL : item.getType();
            String key = normalized.toLowerCase(Locale.ROOT) + "|" + type.name();
            Integer firstIndex = seen.get(key);
            if (firstIndex != null) {
                String fieldPath = "items[" + i + "].name";
                bindingResult.addError(new FieldError("form", fieldPath, message));
                if (flagged.add(firstIndex)) {
                    String firstFieldPath = "items[" + firstIndex + "].name";
                    bindingResult.addError(new FieldError("form", firstFieldPath, message));
                }
            } else {
                seen.put(key, i);
            }
        }
    }

    private void addDuplicateCertificateErrors(CertificateForm form, BindingResult bindingResult) {
        if (form == null || form.getItems() == null || form.getItems().isEmpty()) {
            return;
        }
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                "certificate.duplicate",
                null,
                "Certificate with the same name and issuer already exists.",
                locale
        );
        Map<String, Integer> seen = new LinkedHashMap<>();
        java.util.Set<Integer> flagged = new java.util.HashSet<>();
        List<Certificate> items = form.getItems();
        for (int i = 0; i < items.size(); i++) {
            Certificate item = items.get(i);
            if (item == null) {
                continue;
            }
            String nameKey = normalizeCertificateKeyPart(item.getName());
            String issuerKey = normalizeCertificateKeyPart(item.getIssuer());
            if (!StringUtils.hasText(nameKey) || !StringUtils.hasText(issuerKey)) {
                continue;
            }
            String key = nameKey + "|" + issuerKey;
            Integer firstIndex = seen.get(key);
            if (firstIndex != null) {
                String fieldPath = "items[" + i + "].name";
                bindingResult.addError(new FieldError("form", fieldPath, message));
                if (flagged.add(firstIndex)) {
                    String firstFieldPath = "items[" + firstIndex + "].name";
                    bindingResult.addError(new FieldError("form", firstFieldPath, message));
                }
            } else {
                seen.put(key, i);
            }
        }
    }

    private boolean isPracticEmpty(Practic item) {
        if (item == null) {
            return true;
        }
        boolean hasPosition = StringUtils.hasText(item.getPosition());
        boolean hasCompany = StringUtils.hasText(item.getCompany());
        boolean hasResponsibilities = StringUtils.hasText(item.getResponsibilities());
        boolean hasBeginDate = item.getBeginDate() != null;
        return !(hasPosition || hasCompany || hasResponsibilities || hasBeginDate);
    }

    private boolean isEducationEmpty(Education item) {
        if (item == null) {
            return true;
        }
        boolean hasUniversity = StringUtils.hasText(item.getUniversity());
        boolean hasFaculty = StringUtils.hasText(item.getFaculty());
        boolean hasSummary = StringUtils.hasText(item.getSummary());
        return !(hasUniversity || hasFaculty || hasSummary);
    }

    private boolean isCertificateEmpty(Certificate item) {
        if (item == null) {
            return true;
        }
        boolean hasName = StringUtils.hasText(item.getName());
        boolean hasIssuer = StringUtils.hasText(item.getIssuer());
        boolean hasSmallUrl = StringUtils.hasText(item.getSmallUrl());
        boolean hasLargeUrl = StringUtils.hasText(item.getLargeUrl());
        return !(hasName || hasIssuer || hasSmallUrl || hasLargeUrl);
    }

    private String normalizeCertificateKeyPart(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String prepareProfileMain(String uid, Model model) {
        Profile profile = resolveProfile(uid);
        if (profile == null) {
            return "redirect:/login";
        }
        ProfileMainForm form = new ProfileMainForm();
        form.setBirthDay(profile.getBirthDay());
        form.setCountry(profile.getCountry());
        form.setCity(profile.getCity());
        form.setEmail(profile.getEmail());
        form.setPhone(profile.getPhone());
        form.setObjective(profile.getObjective());
        form.setSummary(profile.getSummary());
        form.setInfo(profile.getInfo());
        model.addAttribute("profile", profile);
        model.addAttribute("form", form);
        return "edit/profile";
    }

    private String prepareProfileModel(String uid, Model model, String viewName, Object form) {
        Profile profile = resolveProfile(uid);
        if (profile == null) {
            return "redirect:/login";
        }
        return prepareProfileModel(profile, model, viewName, form);
    }

    private String prepareProfileModel(Profile profile, Model model, String viewName, Object form) {
        model.addAttribute("profile", profile);
        model.addAttribute("form", formFromProfile(form, profile));
        return viewName;
    }

    private Profile resolveProfile(String uid) {
        CurrentProfile current = currentProfileProvider.getCurrentProfile();
        if (current == null) {
            return null;
        }
        if (!current.getUsername().equals(uid)) {
            throw new AccessDeniedException("Access denied to profile");
        }
        return profileService.findByIdWithAll(current.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    private Object formFromProfile(Object emptyForm, Profile profile) {
        if (emptyForm instanceof SkillForm skillForm) {
            skillForm.setItems(profile.getSkills());
            return skillForm;
        }
        if (emptyForm instanceof PracticForm practicForm) {
            practicForm.setItems(profile.getPractics());
            return practicForm;
        }
        if (emptyForm instanceof EducationForm educationForm) {
            educationForm.setItems(profile.getEducations());
            return educationForm;
        }
        if (emptyForm instanceof CourseForm courseForm) {
            courseForm.setItems(profile.getCourses());
            return courseForm;
        }
        if (emptyForm instanceof LanguageForm languageForm) {
            languageForm.setItems(profile.getLanguages());
            return languageForm;
        }
        if (emptyForm instanceof CertificateForm certificateForm) {
            certificateForm.setItems(profile.getCertificates());
            return certificateForm;
        }
        if (emptyForm instanceof HobbyForm hobbyForm) {
            java.util.List<Long> ids = profile.getHobbies() == null ? java.util.List.of()
                    : profile.getHobbies().stream().map(Hobby::getId).toList();
            hobbyForm.setHobbyIds(ids);
            return hobbyForm;
        }
        if (emptyForm instanceof ContactsForm contactsForm) {
            if (profile.getContacts() == null) {
                profile.setContacts(new net.devstudy.resume.entity.Contacts());
            }
            contactsForm.setPhone(profile.getPhone());
            contactsForm.setEmail(profile.getEmail());
            contactsForm.setFacebook(profile.getContacts().getFacebook());
            contactsForm.setLinkedin(profile.getContacts().getLinkedin());
            contactsForm.setGithub(profile.getContacts().getGithub());
            contactsForm.setStackoverflow(profile.getContacts().getStackoverflow());
            return contactsForm;
        }
        if (emptyForm instanceof InfoForm infoForm) {
            infoForm.setBirthDay(profile.getBirthDay());
            infoForm.setCountry(profile.getCountry());
            infoForm.setCity(profile.getCity());
            infoForm.setObjective(profile.getObjective());
            infoForm.setSummary(profile.getSummary());
            infoForm.setInfo(profile.getInfo());
            return infoForm;
        }
        return emptyForm;
    }
}
