package com.taskm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.entity.Strategy;
import com.taskm.mapper.StrategyMapper;
import com.taskm.service.StrategyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for StrategyController.
 * Tests REST API endpoints end-to-end.
 */
@SpringBootTest(classes = com.taskm.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StrategyMapper strategyMapper;

    @Autowired
    private StrategyService strategyService;

    private Strategy testStrategy1;
    private Strategy testStrategy2;

    @BeforeEach
    void setUp() {
        // Clean up database
        strategyMapper.delete(null);

        // Create test data
        testStrategy1 = new Strategy();
        testStrategy1.setName("test-python-strategy");
        testStrategy1.setDescription("Test Python strategy");
        testStrategy1.setLanguage("python");
        testStrategy1.setCode("print('test')");
        strategyService.save(testStrategy1);

        testStrategy2 = new Strategy();
        testStrategy2.setName("test-js-strategy");
        testStrategy2.setDescription("Test JavaScript strategy");
        testStrategy2.setLanguage("javascript");
        testStrategy2.setCode("console.log('test');");
        strategyService.save(testStrategy2);
    }

    @Test
    void canGetAllStrategies() throws Exception {
        mockMvc.perform(get("/api/strategies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void canGetStrategyById() throws Exception {
        mockMvc.perform(get("/api/strategies/{id}", testStrategy1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testStrategy1.getId()))
                .andExpect(jsonPath("$.data.name").value("test-python-strategy"))
                .andExpect(jsonPath("$.data.language").value("python"));
    }

    @Test
    void canFilterStrategiesByLanguage() throws Exception {
        mockMvc.perform(get("/api/strategies")
                        .param("language", "python")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].language").value("python"));
    }

    @Test
    void shouldReturn404WhenStrategyNotFound() throws Exception {
        mockMvc.perform(get("/api/strategies/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
