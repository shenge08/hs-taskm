package com.taskm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.entity.Listener;
import com.taskm.mapper.ListenerMapper;
import com.taskm.service.ListenerService;
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
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for ListenerController.
 * Tests REST API endpoints end-to-end.
 */
@SpringBootTest(classes = com.taskm.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ListenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ListenerMapper listenerMapper;

    @Autowired
    private ListenerService listenerService;

    private Listener testListener1;
    private Listener testListener2;

    @BeforeEach
    void setUp() {
        // Clean up database
        listenerMapper.delete(null);

        // Create test data
        testListener1 = new Listener();
        testListener1.setName("email-notification-listener");
        testListener1.setDescription("Send email notifications on task completion");
        testListener1.setEventType("TASK_COMPLETED");
        testListener1.setLanguage("python");
        testListener1.setCode("def send_notification(task): send_email(task)");
        testListener1.setEnabled(true);

        Map<String, Object> params1 = new HashMap<>();
        params1.put("parameters", List.of(
            Map.of("name", "recipients", "type", "array", "default", List.of("admin@example.com"), "required", true)
        ));
        testListener1.setParameters(params1);

        Map<String, Object> defaults1 = new HashMap<>();
        defaults1.put("recipients", List.of("admin@example.com"));
        testListener1.setParameterDefaults(defaults1);

        testListener2 = new Listener();
        testListener2.setName("webhook-listener");
        testListener2.setDescription("Send webhook notifications on signal generation");
        testListener2.setEventType("SIGNAL_GENERATED");
        testListener2.setLanguage("javascript");
        testListener2.setCode("function sendWebhook(signal) { /* webhook code */ }");
        testListener2.setEnabled(false);

        Map<String, Object> params2 = new HashMap<>();
        params2.put("parameters", List.of(
            Map.of("name", "url", "type", "string", "required", true)
        ));
        testListener2.setParameters(params2);

        Map<String, Object> defaults2 = new HashMap<>();
        defaults2.put("url", "https://example.com/webhook");
        testListener2.setParameterDefaults(defaults2);
    }

    @Test
    void canGetAllListeners() throws Exception {
        listenerService.save(testListener1);
        listenerService.save(testListener2);

        mockMvc.perform(get("/api/listeners")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void canGetListenerById() throws Exception {
        listenerService.save(testListener1);

        mockMvc.perform(get("/api/listeners/{id}", testListener1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testListener1.getId()))
                .andExpect(jsonPath("$.data.name").value("email-notification-listener"))
                .andExpect(jsonPath("$.data.language").value("python"));
    }

    @Test
    void canFilterListenersByLanguage() throws Exception {
        listenerService.save(testListener1);
        listenerService.save(testListener2);

        mockMvc.perform(get("/api/listeners")
                        .param("language", "python")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].language").value("python"));
    }

    @Test
    void canFilterListenersByEventType() throws Exception {
        listenerService.save(testListener1);
        listenerService.save(testListener2);

        mockMvc.perform(get("/api/listeners")
                        .param("eventType", "SIGNAL_GENERATED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].eventType").value("SIGNAL_GENERATED"));
    }

    @Test
    void shouldReturn404WhenListenerNotFound() throws Exception {
        mockMvc.perform(get("/api/listeners/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
