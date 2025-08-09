package net.devstudy.resume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping({ "/", "/index" })
    public String root() {
        return "redirect:/welcome";
    }
}