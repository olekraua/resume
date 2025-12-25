package net.devstudy.resume.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

class UploadResourceConfigTest {

    @Test
    void registersUploadsResourceHandler() {
        UploadResourceConfig config = new UploadResourceConfig();
        ResourceHandlerRegistry registry = new ResourceHandlerRegistry(
                new StaticApplicationContext(),
                new MockServletContext());

        config.addResourceHandlers(registry);

        assertTrue(registry.hasMappingForPattern("/uploads/**"));

        @SuppressWarnings("unchecked")
        List<ResourceHandlerRegistration> registrations =
                (List<ResourceHandlerRegistration>) ReflectionTestUtils.getField(registry, "registrations");

        assertNotNull(registrations);

        ResourceHandlerRegistration registration = registrations.stream()
                .filter(item -> {
                    String[] patterns = (String[]) ReflectionTestUtils.getField(item, "pathPatterns");
                    return patterns != null && patterns.length == 1 && "/uploads/**".equals(patterns[0]);
                })
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        List<String> locations = (List<String>) ReflectionTestUtils.getField(registration, "locationValues");

        assertNotNull(locations);
        assertEquals(List.of("file:uploads/"), locations);
    }
}
