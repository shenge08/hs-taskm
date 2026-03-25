package com.taskm.listener.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.listener.sample.dto.TaskCompletedRequest;
import com.taskm.listener.sample.dto.TaskFailedRequest;
import com.taskm.listener.sample.dto.TaskStartedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ListenerController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ListenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testInfoEndpoint() throws Exception {
        mockMvc.perform(get("/api/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("TaskM Listener Sample"))
            .andExpect(jsonPath("$.version").value("1.0.0"))
            .andExpect(jsonPath("$.endpoints").exists());
    }

    @Test
    void testTaskStartedEndpoint() throws Exception {
        TaskStartedRequest request = new TaskStartedRequest();
        request.setTaskId(123L);
        request.setTimestamp(Instant.now());
        request.setMessage("Task started");

        mockMvc.perform(post("/api/task-started")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Task started event received"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testTaskCompletedEndpoint() throws Exception {
        TaskCompletedRequest request = new TaskCompletedRequest();
        request.setTaskId(123L);
        request.setTimestamp(Instant.now());
        request.setResult(Map.of(
            "profit", 100.5,
            "trades", 5,
            "winRate", 0.6
        ));
        request.setMessage("Task completed successfully");

        mockMvc.perform(post("/api/task-completed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Task completed event received"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testTaskFailedEndpoint() throws Exception {
        TaskFailedRequest request = new TaskFailedRequest();
        request.setTaskId(123L);
        request.setTimestamp(Instant.now());
        request.setError("Connection timeout");
        request.setStackTrace("java.net.SocketTimeoutException: Read timed out");

        mockMvc.perform(post("/api/task-failed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Task failed event received"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
