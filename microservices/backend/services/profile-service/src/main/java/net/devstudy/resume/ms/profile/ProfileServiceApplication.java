package net.devstudy.resume.ms.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import net.devstudy.resume.web.controller.ProfileApiController;
import net.devstudy.resume.web.controller.api.ProfileConnectionApiController;
import net.devstudy.resume.web.controller.api.ProfileEditApiController;

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan(basePackages = "net.devstudy.resume")
@ComponentScan(basePackages = {
        "net.devstudy.resume.profile",
        "net.devstudy.resume.media",
        "net.devstudy.resume.staticdata",
        "net.devstudy.resume.search",
        "net.devstudy.resume.shared",
        "net.devstudy.resume.web.security",
        "net.devstudy.resume.web.config"
})
@Import({
        ProfileApiController.class,
        ProfileEditApiController.class,
        ProfileConnectionApiController.class
})
public class ProfileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }
}
