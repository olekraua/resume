package net.devstudy.resume.controller;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.Hobby;
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
import net.devstudy.resume.service.ProfileService;
import net.devstudy.resume.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/edit")
@RequiredArgsConstructor
public class EditProfileController {

    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String editRoot() {
        return "redirect:/edit/skills";
    }

    @GetMapping("/skills")
    public String editSkills(Model model) {
        return prepareSkills(model);
    }

    @GetMapping("/practics")
    public String editPractics(Model model) {
        return preparePractics(model);
    }

    @GetMapping("/education")
    public String editEducation(Model model) {
        return prepareEducation(model);
    }

    @GetMapping("/courses")
    public String editCourses(Model model) {
        return prepareCourses(model);
    }

    @GetMapping("/languages")
    public String editLanguages(Model model) {
        return prepareLanguages(model);
    }

    @GetMapping("/hobbies")
    public String editHobbies(Model model) {
        return prepareHobbies(model);
    }

    @GetMapping("/contacts")
    public String editContacts(Model model) {
        return prepareContacts(model);
    }

    @GetMapping("/info")
    public String editInfo(Model model) {
        return prepareInfo(model);
    }

    @GetMapping("/password")
    public String editPassword(Model model) {
        return preparePassword(model);
    }

    @PostMapping("/skills")
    public String saveSkills(@Valid @ModelAttribute("form") SkillForm form, BindingResult bindingResult, Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareSkills(model);
        }
        profileService.updateSkills(profileId, form.getItems());
        return "redirect:/edit/skills?success";
    }

    @PostMapping("/practics")
    public String savePractics(@Valid @ModelAttribute("form") PracticForm form, BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return preparePractics(model);
        }
        profileService.updatePractics(profileId, form.getItems());
        return "redirect:/edit/practics?success";
    }

    @PostMapping("/education")
    public String saveEducation(@Valid @ModelAttribute("form") EducationForm form, BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareEducation(model);
        }
        profileService.updateEducations(profileId, form.getItems());
        return "redirect:/edit/education?success";
    }

    @PostMapping("/courses")
    public String saveCourses(@Valid @ModelAttribute("form") CourseForm form, BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareCourses(model);
        }
        profileService.updateCourses(profileId, form.getItems());
        return "redirect:/edit/courses?success";
    }

    @PostMapping("/languages")
    public String saveLanguages(@Valid @ModelAttribute("form") LanguageForm form, BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareLanguages(model);
        }
        profileService.updateLanguages(profileId, form.getItems());
        return "redirect:/edit/languages?success";
    }

    @PostMapping("/hobbies")
    public String saveHobbies(@Valid @ModelAttribute("form") HobbyForm form, BindingResult bindingResult, Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareHobbies(model);
        }
        profileService.updateHobbies(profileId, form.getHobbyIds());
        return "redirect:/edit/hobbies?success";
    }

    @PostMapping("/contacts")
    public String saveContacts(@Valid @ModelAttribute("form") ContactsForm form, BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareContacts(model);
        }
        profileService.updateContacts(profileId, form);
        return "redirect:/edit/contacts?success";
    }

    @PostMapping("/info")
    public String saveInfo(@Valid @ModelAttribute("form") InfoForm form, BindingResult bindingResult, Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return prepareInfo(model);
        }
        profileService.updateInfo(profileId, form);
        return "redirect:/edit/info?success";
    }

    @PostMapping("/password")
    public String savePassword(@Valid @ModelAttribute("form") ChangePasswordForm form, BindingResult bindingResult,
            Model model) {
        Long profileId = SecurityUtil.getCurrentId();
        if (profileId == null)
            return "redirect:/login";
        if (bindingResult.hasErrors()) {
            return preparePassword(model);
        }
        Profile profile = profileService.findById(profileId).orElse(null);
        if (profile == null) {
            return "redirect:/login";
        }
        if (!passwordEncoder.matches(form.getCurrentPassword(), profile.getPassword())) {
            bindingResult.rejectValue("currentPassword", "password.mismatch", "Невірний поточний пароль");
            return preparePassword(model);
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.confirm", "Паролі не співпадають");
            return preparePassword(model);
        }
        profileService.updatePassword(profileId, form.getNewPassword());
        return "redirect:/edit/password?success";
    }

    private String prepareSkills(Model model) {
        return prepareProfileModel(model, "edit/skills", new SkillForm());
    }

    private String preparePractics(Model model) {
        return prepareProfileModel(model, "edit/practics", new PracticForm());
    }

    private String prepareEducation(Model model) {
        return prepareProfileModel(model, "edit/education", new EducationForm());
    }

    private String prepareCourses(Model model) {
        return prepareProfileModel(model, "edit/courses", new CourseForm());
    }

    private String prepareLanguages(Model model) {
        return prepareProfileModel(model, "edit/languages", new LanguageForm());
    }

    private String prepareHobbies(Model model) {
        return prepareProfileModel(model, "edit/hobbies", new HobbyForm());
    }

    private String prepareContacts(Model model) {
        return prepareProfileModel(model, "edit/contacts", new ContactsForm());
    }

    private String prepareInfo(Model model) {
        return prepareProfileModel(model, "edit/info", new InfoForm());
    }

    private String preparePassword(Model model) {
        return prepareProfileModel(model, "edit/password", new ChangePasswordForm());
    }

    private String prepareProfileModel(Model model, String viewName, Object form) {
        CurrentProfile current = SecurityUtil.getCurrentProfile();
        if (current == null) {
            return "redirect:/login";
        }
        Profile profile = profileService.findById(current.getId()).orElse(null);
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
