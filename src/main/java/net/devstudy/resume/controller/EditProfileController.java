package net.devstudy.resume.controller;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.Hobby;
import net.devstudy.resume.entity.SkillCategory;
import net.devstudy.resume.form.ChangePasswordForm;
import net.devstudy.resume.form.ContactsForm;
import net.devstudy.resume.form.CourseForm;
import net.devstudy.resume.form.EducationForm;
import net.devstudy.resume.form.HobbyForm;
import net.devstudy.resume.form.InfoForm;
import net.devstudy.resume.form.LanguageForm;
import net.devstudy.resume.form.PracticForm;
import net.devstudy.resume.form.SkillForm;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.model.LanguageLevel;
import net.devstudy.resume.model.LanguageType;
import net.devstudy.resume.model.UploadCertificateResult;
import net.devstudy.resume.service.ProfileService;
import net.devstudy.resume.service.CertificateStorageService;
import net.devstudy.resume.service.PhotoStorageService;
import net.devstudy.resume.service.StaticDataService;
import net.devstudy.resume.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.List;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/{uid}/edit")
@RequiredArgsConstructor
public class EditProfileController {

    private final ProfileService profileService;
    private final StaticDataService staticDataService;
    private final CertificateStorageService certificateStorageService;
    private final PhotoStorageService photoStorageService;
    private final PasswordEncoder passwordEncoder;

    @ModelAttribute("skillCategories")
    public java.util.List<SkillCategory> skillCategories() {
        return staticDataService.findSkillCategories();
    }

    @GetMapping
    public String editRoot(@PathVariable String uid) {
        return "redirect:/" + uid + "/edit/skills";
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
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(LanguageType.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null || text.isBlank() ? null : LanguageType.valueOf(text.trim()));
            }
        });
        binder.registerCustomEditor(LanguageLevel.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null || text.isBlank() ? null : LanguageLevel.valueOf(text.trim()));
            }
        });
    }

    @PostMapping("/skills")
    public String saveSkills(@PathVariable String uid, @Valid @ModelAttribute("form") SkillForm form,
            BindingResult bindingResult, Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareSkills(uid, model);
        }
        profileService.updateSkills(profileId, form.getItems());
        return "redirect:/" + uid + "/edit/skills?success";
    }

    @PostMapping("/practics")
    public String savePractics(@PathVariable String uid, @Valid @ModelAttribute("form") PracticForm form,
            BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return preparePractics(uid, model);
        }
        profileService.updatePractics(profileId, form.getItems());
        return "redirect:/" + uid + "/edit/practics?success";
    }

    @PostMapping("/education")
    public String saveEducation(@PathVariable String uid, @Valid @ModelAttribute("form") EducationForm form,
            BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareEducation(uid, model);
        }
        profileService.updateEducations(profileId, form.getItems());
        return "redirect:/" + uid + "/edit/education?success";
    }

    @PostMapping("/courses")
    public String saveCourses(@PathVariable String uid, @Valid @ModelAttribute("form") CourseForm form,
            BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareCourses(uid, model);
        }
        profileService.updateCourses(profileId, form.getItems());
        return "redirect:/" + uid + "/edit/courses?success";
    }

    @PostMapping("/languages")
    public String saveLanguages(@PathVariable String uid, @Valid @ModelAttribute("form") LanguageForm form,
            BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareLanguages(uid, model);
        }
        profileService.updateLanguages(profileId, form.getItems());
        return "redirect:/" + uid + "/edit/languages?success";
    }

    @PostMapping("/certificates/upload")
    @ResponseBody
    public UploadCertificateResult uploadCertificate(@RequestParam("certificateFile") MultipartFile certificateFile) {
        return certificateStorageService.store(certificateFile);
    }

    @PostMapping("/photo")
    public String uploadPhoto(@PathVariable String uid, @RequestParam("profilePhoto") MultipartFile profilePhoto) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        String[] urls = photoStorageService.store(profilePhoto);
        profileService.updatePhoto(profileId, urls[0], urls[1]);
        return "redirect:/" + uid + "/edit/photo?success";
    }

    @PostMapping("/certificates")
    public String saveCertificates(@PathVariable String uid,
            @Valid @ModelAttribute("form") net.devstudy.resume.form.CertificateForm form,
            BindingResult bindingResult, Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareCertificates(uid, model);
        }
        profileService.updateCertificates(profileId, form.getItems());
        return "redirect:/" + uid + "/edit/certificates?success";
    }

    @PostMapping("/hobbies")
    public String saveHobbies(@PathVariable String uid, @ModelAttribute("form") HobbyForm form,
            @RequestParam(value = "hobbies", required = false) String hobbiesParam,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        List<Long> ids = form.getHobbyIds();
        if ((ids == null || ids.isEmpty()) && StringUtils.hasText(hobbiesParam)) {
            try {
                ids = java.util.Arrays.stream(hobbiesParam.split(","))
                        .filter(s -> !s.isBlank())
                        .map(Long::valueOf)
                        .toList();
            } catch (NumberFormatException ex) {
                model.addAttribute("hobbyError", "Невірний формат ідентифікатора хобі");
                return prepareHobbies(uid, model);
            }
        }
        if (ids == null || ids.isEmpty()) {
            model.addAttribute("hobbyError", "Оберіть хоча б одне хобі");
            return prepareHobbies(uid, model);
        }
        profileService.updateHobbies(profileId, ids);
        return "redirect:/" + uid + "/edit/hobbies?success";
    }

    @PostMapping("/contacts")
    public String saveContacts(@PathVariable String uid, @Valid @ModelAttribute("form") ContactsForm form,
            BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareContacts(uid, model);
        }
        profileService.updateContacts(profileId, form);
        return "redirect:/" + uid + "/edit/contacts?success";
    }

    @PostMapping("/info")
    public String saveInfo(@PathVariable String uid, @Valid @ModelAttribute("form") InfoForm form,
            BindingResult bindingResult, Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareInfo(uid, model);
        }
        profileService.updateInfo(profileId, form);
        return "redirect:/" + uid + "/edit/info?success";
    }

    @PostMapping("/password")
    public String savePassword(@PathVariable String uid, @Valid @ModelAttribute("form") ChangePasswordForm form,
            BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return preparePassword(uid, model);
        }
        Profile profile = profileService.findById(profileId).orElse(null);
        if (profile == null) {
            return "redirect:/login";
        }
        if (!passwordEncoder.matches(form.getCurrentPassword(), profile.getPassword())) {
            bindingResult.rejectValue("currentPassword", "password.mismatch", "Невірний поточний пароль");
            return preparePassword(uid, model);
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.confirm", "Паролі не співпадають");
            return preparePassword(uid, model);
        }
        profileService.updatePassword(profileId, form.getNewPassword());
        return "redirect:/" + uid + "/edit/password?success";
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
        return prepareProfileModel(uid, model, "edit/languages", new LanguageForm());
    }

    private String prepareCertificates(String uid, Model model) {
        return prepareProfileModel(uid, model, "edit/certificates", new net.devstudy.resume.form.CertificateForm());
    }

    private String preparePhoto(String uid, Model model) {
        return prepareProfileModel(uid, model, "edit/photo", null);
    }

    private String prepareHobbies(String uid, Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        HobbyForm form = new HobbyForm();
        if (profileId != null) {
            profileService.findById(profileId).ifPresent(profile -> {
                if (profile.getHobbies() != null) {
                    form.setHobbyIds(profile.getHobbies().stream().map(Hobby::getId).toList());
                }
            });
        }
        model.addAttribute("hobbies", staticDataService.findAllHobbiesWithSelected(form.getHobbyIds()));
        return prepareProfileModel(uid, model, "edit/hobbies", form);
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

    private String prepareProfileModel(String uid, Model model, String viewName, Object form) {
        CurrentProfile current = SecurityUtil.getCurrentProfile();
        if (current == null || !current.getUsername().equals(uid)) {
            return "redirect:/login";
        }
        Profile profile = profileService.findByIdWithAll(current.getId()).orElse(null);
        if (profile == null) {
            return "redirect:/login";
        }
        model.addAttribute("profile", profile);
        model.addAttribute("form", formFromProfile(form, profile));
        return viewName;
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
