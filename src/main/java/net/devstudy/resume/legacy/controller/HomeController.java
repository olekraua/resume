package net.devstudy.resume.legacy.controller;

//import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//@Controller
@Deprecated
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "index"; // verweist auf src/main/resources/templates/index.html
    }


}
