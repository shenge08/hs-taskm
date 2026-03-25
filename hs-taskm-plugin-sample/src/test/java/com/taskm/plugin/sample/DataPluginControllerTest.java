package com.taskm.plugin.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 数据插件控制器测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
class DataPluginControllerTest {

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
            .andExpect(jsonPath("$.name").value("TaskM Data Plugin Sample"))
            .andExpect(jsonPath("$.version").value("1.0.0"))
            .andExpect(jsonPath("$.endpoints").exists());
    }

    @Test
    void testGetDataEndpoint() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", "BTC/USDT");
        params.put("interval", "1h");

        mockMvc.perform(post("/api/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.symbol").value("BTC/USDT"))
            .andExpect(jsonPath("$.data.interval").value("1h"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetMultiDataEndpoint() throws Exception {
        String requestJson = """
            {
                "symbol": "BTC/USDT",
                "interval": "1h",
                "limit": 5
            }
            """;

        mockMvc.perform(post("/api/data/multi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.symbol").value("BTC/USDT"))
            .andExpect(jsonPath("$.data.count").value(5))
            .andExpect(jsonPath("$.data.data").isArray());
    }

    @Test
    void testMarketInfoEndpoint() throws Exception {
        mockMvc.perform(get("/api/market/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exchange").exists())
            .andExpect(jsonPath("$.symbols").isArray())
            .andExpect(jsonPath("$.intervals").isArray());
    }
}
