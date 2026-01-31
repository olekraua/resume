package net.devstudy.resume.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "app.ui.spa.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.ui.mvc.enabled", havingValue = "false", matchIfMissing = true)
public class SpaForwardingConfig implements WebMvcConfigurer {

    private static final String SPA_PATH_REGEX =
            "^(?!api|uploads|css|js|img|fonts|favicon|media|actuator|error|assets)(?!.*\\.).*$";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/{path:" + SPA_PATH_REGEX + "}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path:" + SPA_PATH_REGEX + "}/**")
                .setViewName("forward:/index.html");
        registry.setOrder(Ordered.LOWEST_PRECEDENCE);
    }
}
