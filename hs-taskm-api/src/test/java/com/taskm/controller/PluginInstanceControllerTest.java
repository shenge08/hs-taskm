package com.taskm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.entity.DataPlugin;
import com.taskm.mapper.DataPluginMapper;
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
 * Integration test for PluginInstanceController.
 * Tests REST API endpoints end-to-end.
 */
@SpringBootTest(classes = com.taskm.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PluginInstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataPluginMapper dataPluginMapper;

    private DataPlugin testPlugin;

    @BeforeEach
    void setUp() {
        // Clean up database
        dataPluginMapper.delete(null);

        // Create test plugin
        testPlugin = new DataPlugin();
        testPlugin.setName("stock-quote-plugin");
        testPlugin.setDescription("Get stock quotes");
        testPlugin.setPluginType("market_data");
        testPlugin.setLanguage("python");
        testPlugin.setCode("def get_stock_quote(symbol): return {'symbol': symbol, 'price': 100.0}");

        Map<String, Object> params = new HashMap<>();
        params.put("parameters", java.util.List.of(
            Map.of("name", "symbol", "type", "string", "default", "AAPL", "required", true)
        ));
        testPlugin.setConfigParameters(params);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", "John Doe");
        metadata.put("version", "1.0");
        testPlugin.setMetadata(metadata);

        dataPluginMapper.insert(testPlugin);
    }

    @Test
    void canCreateInstance() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "test-instance");
        requestBody.put("isDefault", false);

        Map<String, Object> config = new HashMap<>();
        config.put("apiKey", "test-key");
        requestBody.put("config", config);

        mockMvc.perform(post("/api/plugins/" + testPlugin.getId() + "/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("test-instance"))
                .andExpect(jsonPath("$.data.config.apiKey").value("test-key"));
    }

    @Test
    void canGetInstances() throws Exception {
        mockMvc.perform(get("/api/plugins/" + testPlugin.getId() + "/instances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void cannotCreateDuplicateInstanceName() throws Exception {
        // Create first instance
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "duplicate-name");
        requestBody.put("config", new HashMap<>());

        mockMvc.perform(post("/api/plugins/" + testPlugin.getId() + "/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        // Try to create duplicate
        mockMvc.perform(post("/api/plugins/" + testPlugin.getId() + "/instances")
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

        String response = mockMvc.perform(post("/api/plugins/" + testPlugin.getId() + "/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn().getResponse().getContentAsString();

        // Extract instance ID from response
        String instanceId = objectMapper.readTree(response).path("data").path("id").asText();

        // Set as default
        mockMvc.perform(post("/api/plugins/" + testPlugin.getId() + "/instances/" + instanceId + "/setDefault"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
