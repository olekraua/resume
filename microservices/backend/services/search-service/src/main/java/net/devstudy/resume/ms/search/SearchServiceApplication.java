package net.devstudy.resume.ms.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

import net.devstudy.resume.search.internal.web.SearchQueryApiController;

@SpringBootApplication
@EnableRabbit
@EnableCaching
@ConfigurationPropertiesScan(basePackages = "net.devstudy.resume")
@ComponentScan(basePackages = {
        "net.devstudy.resume.search",
        "net.devstudy.resume.ms.search",
        "net.devstudy.resume.shared",
        "net.devstudy.resume.web.config"
})
@Import({
        SearchQueryApiController.class
})
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
