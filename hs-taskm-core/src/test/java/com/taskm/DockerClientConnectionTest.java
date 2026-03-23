package com.taskm;

import com.github.dockerjava.api.DockerClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that verifies Docker Client can connect to Docker daemon.
 * This ensures the Docker integration is properly configured.
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class DockerClientConnectionTest {

    @Autowired(required = false)
    private DockerClient dockerClient;

    @Test
    void canConnectToDocker() {
        // This test verifies DockerClient is properly injected
        // It will fail (RED) until we configure DockerClient bean
        assertThat(dockerClient).isNotNull();
    }
}
