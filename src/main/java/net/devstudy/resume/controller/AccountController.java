package net.devstudy.resume.controller;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.form.ChangePasswordForm;
import net.devstudy.resume.form.ChangeLoginForm;
import net.devstudy.resume.security.CurrentProfileProvider;
import net.devstudy.resume.service.ProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentProfileProvider currentProfileProvider;

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "auth/change-password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid ChangePasswordForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/change-password";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Новий пароль і підтвердження не збігаються");
            return "auth/change-password";
        }
        Long currentId = currentProfileProvider.getCurrentId();
        if (currentId == null) {
            return "redirect:/login";
        }
        Optional<Profile> profileOpt = profileService.findById(currentId);
        if (profileOpt.isEmpty()
                || !passwordEncoder.matches(form.getCurrentPassword(), profileOpt.get().getPassword())) {
            model.addAttribute("errorMessage", "Невірний поточний пароль");
            return "auth/change-password";
        }
        profileService.updatePassword(currentId, form.getNewPassword());
        return "redirect:/account/password?success";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        Long currentId = currentProfileProvider.getCurrentId();
        if (currentId == null) {
            return "redirect:/login";
        }
        Optional<Profile> profileOpt = profileService.findById(currentId);
        if (profileOpt.isEmpty()) {
            return "redirect:/login";
        }
        ChangeLoginForm form = new ChangeLoginForm();
        form.setNewUid(profileOpt.get().getUid());
        model.addAttribute("currentUid", profileOpt.get().getUid());
        model.addAttribute("changeLoginForm", form);
        return "auth/change-login";
    }

    @PostMapping("/login")
    public String changeLogin(@Valid ChangeLoginForm form, BindingResult bindingResult, Model model) {
        Long currentId = currentProfileProvider.getCurrentId();
        if (currentId == null) {
            return "redirect:/login";
        }
        Optional<Profile> profileOpt = profileService.findById(currentId);
        if (profileOpt.isEmpty()) {
            return "redirect:/login";
        }

        Profile profile = profileOpt.get();

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUid", profile.getUid());
            return "auth/change-login";
        }
        try {
            profileService.updateUid(currentId, form.getNewUid());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("currentUid", profile.getUid());
            model.addAttribute("uidError", ex.getMessage());
            return "auth/change-login";
        }
        return "redirect:/login?loginChanged";
    }
}
