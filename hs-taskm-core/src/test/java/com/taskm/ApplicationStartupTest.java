package com.taskm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that verifies the Spring Boot application can start up successfully.
 * This is a smoke test to ensure all beans and configurations are loaded correctly.
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class ApplicationStartupTest {

    @Test
    void contextLoads() {
        // This test will fail if the Spring context cannot be loaded
        // which indicates misconfiguration in dependencies or beans
        assertThat(true).isTrue();
    }
}
