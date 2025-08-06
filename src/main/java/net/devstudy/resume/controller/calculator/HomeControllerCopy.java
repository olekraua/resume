package net.devstudy.resume.controller.calculator;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeControllerCopy {
    
    @GetMapping("/calculator")
    public String home() {
        return "calculator/index"; // verweist auf src/main/resources/templates/index.html
    }


}
