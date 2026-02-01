package net.devstudy.resume.ms.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import net.devstudy.resume.web.controller.SearchApiController;
import net.devstudy.resume.web.controller.SuggestController;

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan(basePackages = "net.devstudy.resume")
@ComponentScan(basePackages = {
        "net.devstudy.resume.search",
        "net.devstudy.resume.profile",
        "net.devstudy.resume.staticdata",
        "net.devstudy.resume.shared",
        "net.devstudy.resume.web.config"
})
@Import({
        SearchApiController.class,
        SuggestController.class
})
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
