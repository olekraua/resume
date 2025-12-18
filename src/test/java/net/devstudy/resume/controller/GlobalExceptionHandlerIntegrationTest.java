package net.devstudy.resume.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.web.bind.annotation.GetMapping;

import net.devstudy.resume.testcontainers.PostgresIntegrationTest;

@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerIntegrationTest.TestController.class)
@Tag("integration")
class GlobalExceptionHandlerIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersServerErrorViewWhenIllegalStateExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test-illegal"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/server-error"))
                .andExpect(model().attribute("path", "/test-illegal"))
                .andExpect(model().attribute("method", "GET"))
                .andExpect(model().attribute("message", "boom"));
    }

    @Test
    void escapesExceptionMessageToPreventHtmlInjection() throws Exception {
        mockMvc.perform(get("/test-illegal-html"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/server-error"))
                .andExpect(model().attribute("path", "/test-illegal-html"))
                .andExpect(model().attribute("method", "GET"))
                .andExpect(model().attribute("message", "<script>alert(1)</script>"))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Controller
    static class TestController {

        @GetMapping("/test-illegal")
        public String illegal() {
            throw new IllegalStateException("boom");
        }

        @GetMapping("/test-illegal-html")
        public String illegalHtml() {
            throw new IllegalStateException("<script>alert(1)</script>");
        }
    }
}
