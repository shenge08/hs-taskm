package com.taskm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.entity.DataPlugin;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.service.DataPluginService;
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
 * Integration test for DataPluginController.
 * Tests REST API endpoints end-to-end.
 */
@SpringBootTest(classes = com.taskm.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DataPluginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataPluginMapper dataPluginMapper;

    @Autowired
    private DataPluginService dataPluginService;

    private DataPlugin testPlugin1;
    private DataPlugin testPlugin2;

    @BeforeEach
    void setUp() {
        // Clean up database
        dataPluginMapper.delete(null);

        // Create test data
        testPlugin1 = new DataPlugin();
        testPlugin1.setName("stock-quote-plugin");
        testPlugin1.setDescription("Get stock quotes from Yahoo Finance");
        testPlugin1.setPluginType("market_data");
        testPlugin1.setLanguage("python");
        testPlugin1.setCode("def get_stock_quote(symbol): return {'symbol': symbol, 'price': 100.0}");

        Map<String, Object> params1 = new HashMap<>();
        params1.put("parameters", java.util.List.of(
            Map.of("name", "symbol", "type", "string", "default", "AAPL", "required", true)
        ));
        testPlugin1.setConfigParameters(params1);

        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("author", "John Doe");
        metadata1.put("version", "1.0");
        testPlugin1.setMetadata(metadata1);

        testPlugin2 = new DataPlugin();
        testPlugin2.setName("news-sentiment-plugin");
        testPlugin2.setDescription("Analyze news sentiment for stocks");
        testPlugin2.setPluginType("analytics");
        testPlugin2.setLanguage("javascript");
        testPlugin2.setCode("function analyzeSentiment(symbol) { return { symbol, sentiment: 'positive' }; }");

        Map<String, Object> params2 = new HashMap<>();
        params2.put("parameters", java.util.List.of(
            Map.of("name", "symbol", "type", "string", "required", true)
        ));
        testPlugin2.setConfigParameters(params2);

        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("author", "Jane Smith");
        metadata2.put("version", "2.0");
        testPlugin2.setMetadata(metadata2);
    }

    @Test
    void canGetAllPlugins() throws Exception {
        dataPluginService.save(testPlugin1);
        dataPluginService.save(testPlugin2);

        mockMvc.perform(get("/api/plugins")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void canGetPluginById() throws Exception {
        dataPluginService.save(testPlugin1);

        mockMvc.perform(get("/api/plugins/{id}", testPlugin1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testPlugin1.getId()))
                .andExpect(jsonPath("$.data.name").value("stock-quote-plugin"))
                .andExpect(jsonPath("$.data.language").value("python"));
    }

    @Test
    void canFilterPluginsByLanguage() throws Exception {
        dataPluginService.save(testPlugin1);
        dataPluginService.save(testPlugin2);

        mockMvc.perform(get("/api/plugins")
                        .param("language", "python")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].language").value("python"));
    }

    @Test
    void canFilterPluginsByType() throws Exception {
        dataPluginService.save(testPlugin1);
        dataPluginService.save(testPlugin2);

        mockMvc.perform(get("/api/plugins")
                        .param("pluginType", "analytics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].pluginType").value("analytics"));
    }

    @Test
    void canTestPlugin() throws Exception {
        dataPluginService.save(testPlugin1);

        Map<String, Object> testParams = new HashMap<>();
        testParams.put("symbol", "AAPL");

        mockMvc.perform(post("/api/plugins/{id}/test", testPlugin1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.message").value("Plugin testing not yet implemented"));
    }

    @Test
    void shouldReturn404WhenPluginNotFound() throws Exception {
        mockMvc.perform(get("/api/plugins/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
