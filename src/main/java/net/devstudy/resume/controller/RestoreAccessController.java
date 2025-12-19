package net.devstudy.resume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import net.devstudy.resume.form.RestoreAccessForm;
import net.devstudy.resume.form.RestorePasswordForm;
import net.devstudy.resume.service.RestoreAccessService;

@Controller
public class RestoreAccessController {

    private final RestoreAccessService restoreAccessService;

    public RestoreAccessController(RestoreAccessService restoreAccessService) {
        this.restoreAccessService = restoreAccessService;
    }

    @GetMapping("/restore")
    public String restoreForm(Model model) {
        if (!model.containsAttribute("restoreAccessForm")) {
            model.addAttribute("restoreAccessForm", new RestoreAccessForm());
        }
        return "auth/restore";
    }

    @PostMapping("/restore")
    public String restore(@ModelAttribute("restoreAccessForm") @Valid RestoreAccessForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/restore";
        }
        String appHost = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        try {
            String link = restoreAccessService.requestRestore(form.getIdentifier(), appHost);
            redirectAttributes.addFlashAttribute("restoreLink", link);
            return "redirect:/restore/success";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/restore";
        }
    }

    @GetMapping("/restore/success")
    public String restoreSuccess() {
        return "auth/restore-success";
    }

    @GetMapping("/restore/{token}")
    public String restorePasswordForm(@PathVariable String token, Model model) {
        if (restoreAccessService.findProfileByToken(token).isEmpty()) {
            return "redirect:/restore?invalid";
        }
        model.addAttribute("token", token);
        model.addAttribute("restorePasswordForm", new RestorePasswordForm());
        return "auth/restore-password";
    }

    @PostMapping("/restore/{token}")
    public String restorePassword(@PathVariable String token,
            @ModelAttribute("restorePasswordForm") @Valid RestorePasswordForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "auth/restore-password";
        }
        try {
            restoreAccessService.resetPassword(token, form.getPassword());
            return "redirect:/login?restored";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/restore-password";
        }
    }
}
