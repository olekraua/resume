package net.devstudy.resume.ms.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import net.devstudy.resume.web.config.CorsConfig;
import net.devstudy.resume.web.config.CorsProperties;
import net.devstudy.resume.web.security.JwtResourceServerConfig;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "net.devstudy.resume")
@EnableConfigurationProperties(CorsProperties.class)
@ComponentScan(
        basePackages = {
                "net.devstudy.resume.messaging",
                "net.devstudy.resume.shared",
                "net.devstudy.resume.ms.messaging",
                "net.devstudy.resume.web.security"
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtResourceServerConfig.class)
)
@Import({CorsConfig.class})
public class MessagingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingServiceApplication.class, args);
    }
}
