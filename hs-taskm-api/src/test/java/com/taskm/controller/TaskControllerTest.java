package com.taskm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.dto.CreateTaskDTO;
import com.taskm.entity.DataPlugin;
import com.taskm.entity.Listener;
import com.taskm.entity.Strategy;
import com.taskm.entity.Task;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.mapper.ListenerMapper;
import com.taskm.mapper.StrategyMapper;
import com.taskm.service.TaskService;
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
 * Integration test for TaskController.
 * Tests REST API endpoints end-to-end.
 */
@SpringBootTest(classes = com.taskm.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StrategyMapper strategyMapper;

    @Autowired
    private DataPluginMapper dataPluginMapper;

    @Autowired
    private ListenerMapper listenerMapper;

    private Strategy testStrategy;
    private DataPlugin testPlugin;
    private Listener testListener;

    @BeforeEach
    void setUp() {
        // Clean up database
        strategyMapper.delete(null);
        dataPluginMapper.delete(null);
        listenerMapper.delete(null);

        // Create test strategy
        testStrategy = new Strategy();
        testStrategy.setName("test-strategy");
        testStrategy.setDescription("Test strategy");
        testStrategy.setLanguage("python");
        testStrategy.setCode("def strategy(): pass");
        strategyMapper.insert(testStrategy);

        // Create test plugin
        testPlugin = new DataPlugin();
        testPlugin.setName("test-plugin");
        testPlugin.setDescription("Test plugin");
        testPlugin.setPluginType("market_data");
        testPlugin.setLanguage("python");
        testPlugin.setCode("def plugin(): pass");
        dataPluginMapper.insert(testPlugin);

        // Create test listener
        testListener = new Listener();
        testListener.setName("test-listener");
        testListener.setDescription("Test listener");
        testListener.setEventType("TASK_COMPLETED");
        testListener.setLanguage("python");
        testListener.setCode("def listener(): pass");
        listenerMapper.insert(testListener);
    }

    @Test
    void canCreateTask() throws Exception {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId());

        Map<String, Object> strategyParams = new HashMap<>();
        strategyParams.put("param1", "value1");
        dto.setStrategyParams(strategyParams);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Task created successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$..data.status").value("PENDING"));
    }

    @Test
    void canCreateTaskWithPluginAndListener() throws Exception {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId());
        dto.setPluginId(testPlugin.getId());
        dto.setListenerId(testListener.getId());

        Map<String, Object> strategyParams = new HashMap<>();
        strategyParams.put("param1", "value1");
        dto.setStrategyParams(strategyParams);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.parameters.pluginId").value(testPlugin.getId()))
                .andExpect(jsonPath("$.data.parameters.listenerId").value(testListener.getId()));
    }

    @Test
    void shouldReturn400WhenLanguageMismatch() throws Exception {
        // Create plugin with different language
        DataPlugin jsPlugin = new DataPlugin();
        jsPlugin.setName("js-plugin");
        jsPlugin.setDescription("JS Plugin");
        jsPlugin.setPluginType("market_data");
        jsPlugin.setLanguage("javascript");
        jsPlugin.setCode("function plugin() {}");
        dataPluginMapper.insert(jsPlugin);

        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId()); // Python
        dto.setPluginId(jsPlugin.getId()); // JavaScript
        dto.setStrategyParams(new HashMap<>());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void canGetAllTasks() throws Exception {
        CreateTaskDTO dto1 = new CreateTaskDTO();
        dto1.setStrategyId(testStrategy.getId());
        dto1.setStrategyParams(new HashMap<>());

        CreateTaskDTO dto2 = new CreateTaskDTO();
        dto2.setStrategyId(testStrategy.getId());
        dto2.setStrategyParams(new HashMap<>());

        taskService.createTask(dto1);
        taskService.createTask(dto2);

        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void canGetTaskById() throws Exception {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId());
        dto.setStrategyParams(new HashMap<>());

        Task task = taskService.createTask(dto);

        mockMvc.perform(get("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(task.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
