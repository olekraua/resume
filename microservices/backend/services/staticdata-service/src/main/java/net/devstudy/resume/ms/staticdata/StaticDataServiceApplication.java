package net.devstudy.resume.ms.staticdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import net.devstudy.resume.web.controller.StaticDataApiController;

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan(basePackages = "net.devstudy.resume")
@ComponentScan(basePackages = {
        "net.devstudy.resume.staticdata",
        "net.devstudy.resume.shared",
        "net.devstudy.resume.web.config"
})
@Import(StaticDataApiController.class)
public class StaticDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StaticDataServiceApplication.class, args);
    }
}
