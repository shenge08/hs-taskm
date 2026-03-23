package com.taskm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.entity.Listener;
import com.taskm.mapper.ListenerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for ListenerInstanceController.
 * Tests REST API endpoints end-to-end.
 */
@SpringBootTest(classes = com.taskm.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ListenerInstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ListenerMapper listenerMapper;

    private Listener testListener;

    @BeforeEach
    void setUp() {
        // Clean up database
        listenerMapper.delete(null);

        // Create test listener
        testListener = new Listener();
        testListener.setName("webhook-listener");
        testListener.setDescription("Webhook listener for events");
        testListener.setListenerType("webhook");
        testListener.setLanguage("javascript");
        testListener.setCode("function handle(event) { console.log(event); }");

        Map<String, Object> params = new HashMap<>();
        params.put("parameters", java.util.List.of(
            Map.of("name", "url", "type", "string", "default", "https://example.com", "required", true)
        ));
        testListener.setConfigParameters(params);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", "Jane Doe");
        metadata.put("version", "1.0");
        testListener.setMetadata(metadata);

        listenerMapper.insert(testListener);
    }

    @Test
    void canCreateInstance() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "test-instance");
        requestBody.put("isDefault", false);

        Map<String, Object> config = new HashMap<>();
        config.put("webhookUrl", "https://example.com/webhook");
        requestBody.put("config", config);

        mockMvc.perform(post("/api/listeners/" + testListener.getId() + "/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("test-instance"))
                .andExpect(jsonPath("$.data.config.webhookUrl").value("https://example.com/webhook"));
    }

    @Test
    void canGetInstances() throws Exception {
        mockMvc.perform(get("/api/listeners/" + testListener.getId() + "/instances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void cannotCreateDuplicateInstanceName() throws Exception {
        // Create first instance
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "duplicate-name");
        requestBody.put("config", new HashMap<>());

        mockMvc.perform(post("/api/listeners/" + testListener.getId() + "/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        // Try to create duplicate
        mockMvc.perform(post("/api/listeners/" + testListener.getId() + "/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void canSetDefaultInstance() throws Exception {
        // Create instance first
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "default-instance");
        requestBody.put("config", new HashMap<>());

        String response = mockMvc.perform(post("/api/listeners/" + testListener.getId() + "/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn().getResponse().getContentAsString();

        // Extract instance ID from response
        String instanceId = objectMapper.readTree(response).path("data").path("id").asText();

        // Set as default
        mockMvc.perform(post("/api/listeners/" + testListener.getId() + "/instances/" + instanceId + "/setDefault"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
