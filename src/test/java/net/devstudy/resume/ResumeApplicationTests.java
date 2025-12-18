package net.devstudy.resume;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import net.devstudy.resume.testcontainers.PostgresIntegrationTest;

@Tag("integration")
class ResumeApplicationTests extends PostgresIntegrationTest {

	@Test
	void contextLoads() {
	}

}
