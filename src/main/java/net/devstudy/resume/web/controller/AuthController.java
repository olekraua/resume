package net.devstudy.resume.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;

import net.devstudy.resume.auth.security.CurrentProfileProvider;
import net.devstudy.resume.auth.model.CurrentProfile;
import net.devstudy.resume.auth.form.RegistrationForm;
import net.devstudy.resume.profile.exception.UidAlreadyExistsException;
import net.devstudy.resume.shared.component.DataBuilder;
import net.devstudy.resume.profile.service.ProfileService;
import net.devstudy.resume.auth.service.UidSuggestionService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    private final ProfileService profileService;
    private final CurrentProfileProvider currentProfileProvider;
    private final UidSuggestionService uidSuggestionService;
    private final DataBuilder dataBuilder;

    public AuthController(ProfileService profileService, CurrentProfileProvider currentProfileProvider,
            UidSuggestionService uidSuggestionService, DataBuilder dataBuilder) {
        this.profileService = profileService;
        this.currentProfileProvider = currentProfileProvider;
        this.uidSuggestionService = uidSuggestionService;
        this.dataBuilder = dataBuilder;
    }

    @GetMapping("/login")
    public String login() {
        if (currentProfileProvider.getCurrentProfile() != null) {
            return "redirect:/me";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (currentProfileProvider.getCurrentProfile() != null) {
            return "redirect:/me";
        }
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @GetMapping("/register/uid-hint")
    @ResponseBody
    public String uidHint(@RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        return dataBuilder.buildProfileUid(firstName, lastName);
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registrationForm") @Valid RegistrationForm form,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request) {
        if (currentProfileProvider.getCurrentProfile() != null) {
            return "redirect:/me";
        }
        if (bindingResult.hasErrors()) {
            return renderRegisterForm(form, bindingResult, model);
        }
        try {
            var profile = profileService.register(form.getUid(), form.getFirstName(), form.getLastName(),
                    form.getPassword());
            CurrentProfile currentProfile = new CurrentProfile(profile);
            var authentication = new UsernamePasswordAuthenticationToken(currentProfile, null,
                    currentProfile.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            return "redirect:/" + profile.getUid();
        } catch (UidAlreadyExistsException ex) {
            bindingResult.rejectValue("uid", "uid.exists");
            model.addAttribute("uidSuggestions", uidSuggestionService.suggest(ex.getUid()));
            return renderRegisterForm(form, bindingResult, model);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return renderRegisterForm(form, bindingResult, model);
        }
    }

    @GetMapping("/me")
    public String me() {
        CurrentProfile currentProfile = currentProfileProvider.getCurrentProfile();
        if (currentProfile == null) {
            return "redirect:/login";
        }
        return "redirect:/" + currentProfile.getUsername();
    }

    private String renderRegisterForm(RegistrationForm form, BindingResult bindingResult, Model model) {
        model.addAttribute("registrationForm", form);
        String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + "registrationForm";
        if (!model.containsAttribute(bindingResultKey)) {
            model.addAttribute(bindingResultKey, bindingResult);
        }
        return "auth/register";
    }
}
