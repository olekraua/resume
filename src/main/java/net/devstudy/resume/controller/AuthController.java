package net.devstudy.resume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.util.SecurityUtil;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/me")
    public String me() {
        CurrentProfile currentProfile = SecurityUtil.getCurrentProfile();
        if (currentProfile == null) {
            return "redirect:/login";
        }
        return "redirect:/" + currentProfile.getUsername();
    }
}
