package com.taskm.service;

import com.taskm.dto.CreateTaskDTO;
import com.taskm.entity.DataPlugin;
import com.taskm.entity.Listener;
import com.taskm.entity.Strategy;
import com.taskm.entity.Task;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.mapper.ListenerMapper;
import com.taskm.mapper.StrategyMapper;
import com.taskm.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TaskService.
 * Tests task creation with validation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private StrategyMapper strategyMapper;

    @Autowired
    private DataPluginMapper dataPluginMapper;

    @Autowired
    private ListenerMapper listenerMapper;

    @Autowired
    private TaskMapper taskMapper;

    private Strategy testStrategy;
    private DataPlugin testPlugin;
    private Listener testListener;

    @BeforeEach
    void setUp() {
        // Clean up database
        taskMapper.delete(null);
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
    void canCreateTaskWithOnlyStrategy() {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId());

        Map<String, Object> strategyParams = new HashMap<>();
        strategyParams.put("param1", "value1");
        dto.setStrategyParams(strategyParams);

        Task task = taskService.createTask(dto);

        assertNotNull(task.getId());
        assertEquals(testStrategy.getId(), task.getStrategyId());
        assertEquals("PENDING", task.getStatus());
        assertNotNull(task.getParameters());
    }

    @Test
    void canCreateTaskWithPlugin() {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId());
        dto.setPluginId(testPlugin.getId());

        Map<String, Object> strategyParams = new HashMap<>();
        strategyParams.put("param1", "value1");
        dto.setStrategyParams(strategyParams);

        Task task = taskService.createTask(dto);

        assertNotNull(task.getId());
        assertEquals(testStrategy.getId(), task.getStrategyId());
        assertNotNull(task.getParameters());
        assertTrue(task.getParameters().containsKey("pluginId"));
        assertTrue(task.getParameters().containsKey("pluginParams"));
    }

    @Test
    void canCreateTaskWithListener() {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId());
        dto.setListenerId(testListener.getId());

        Map<String, Object> strategyParams = new HashMap<>();
        strategyParams.put("param1", "value1");
        dto.setStrategyParams(strategyParams);

        Task task = taskService.createTask(dto);

        assertNotNull(task.getId());
        assertEquals(testStrategy.getId(), task.getStrategyId());
        assertNotNull(task.getParameters());
        assertTrue(task.getParameters().containsKey("listenerId"));
        assertTrue(task.getParameters().containsKey("listenerParams"));
    }

    @Test
    void canCreateTaskWithAllComponents() {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId());
        dto.setPluginId(testPlugin.getId());
        dto.setListenerId(testListener.getId());

        Map<String, Object> strategyParams = new HashMap<>();
        strategyParams.put("param1", "value1");
        dto.setStrategyParams(strategyParams);

        Task task = taskService.createTask(dto);

        assertNotNull(task.getId());
        assertEquals(testStrategy.getId(), task.getStrategyId());
        assertNotNull(task.getParameters());
        assertTrue(task.getParameters().containsKey("strategyId"));
        assertTrue(task.getParameters().containsKey("pluginId"));
        assertTrue(task.getParameters().containsKey("listenerId"));
    }

    @Test
    void shouldThrowExceptionWhenLanguageMismatchPlugin() {
        // Create plugin with different language
        DataPlugin jsPlugin = new DataPlugin();
        jsPlugin.setName("js-plugin");
        jsPlugin.setDescription("JS Plugin");
        jsPlugin.setPluginType("market_data");
        jsPlugin.setLanguage("javascript");
        jsPlugin.setCode("function plugin() {}");
        dataPluginMapper.insert(jsPlugin);

        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId()); // Python strategy
        dto.setPluginId(jsPlugin.getId()); // JS plugin

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(dto);
        });
    }

    @Test
    void shouldThrowExceptionWhenLanguageMismatchListener() {
        // Create listener with different language
        Listener jsListener = new Listener();
        jsListener.setName("js-listener");
        jsListener.setDescription("JS Listener");
        jsListener.setEventType("TASK_COMPLETED");
        jsListener.setLanguage("javascript");
        jsListener.setCode("function listener() {}");
        listenerMapper.insert(jsListener);

        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setStrategyId(testStrategy.getId()); // Python strategy
        dto.setListenerId(jsListener.getId()); // JS listener

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(dto);
        });
    }

    @Test
    void canGetAllTasks() {
        CreateTaskDTO dto1 = new CreateTaskDTO();
        dto1.setStrategyId(testStrategy.getId());
        dto1.setStrategyParams(new HashMap<>());

        CreateTaskDTO dto2 = new CreateTaskDTO();
        dto2.setStrategyId(testStrategy.getId());
        dto2.setStrategyParams(new HashMap<>());

        taskService.createTask(dto1);
        taskService.createTask(dto2);

        List<Task> tasks = taskService.getAllTasks();
        assertEquals(2, tasks.size());
    }

    @Test
    void canGetTasksWithPagination() {
        // Create 5 tasks
        for (int i = 0; i < 5; i++) {
            CreateTaskDTO dto = new CreateTaskDTO();
            dto.setStrategyId(testStrategy.getId());
            dto.setStrategyParams(new HashMap<>());
            taskService.createTask(dto);
        }

        // Test first page
        IPage<Task> page1 = taskService.getTasksWithPagination(0, 2);
        assertEquals(2, page1.getRecords().size());
        assertEquals(0, page1.getCurrent());
        assertEquals(2, page1.getSize());
        assertEquals(5, page1.getTotal());

        // Test second page
        IPage<Task> page2 = taskService.getTasksWithPagination(1, 2);
        assertEquals(2, page2.getRecords().size());
        assertEquals(1, page2.getCurrent());

        // Test last page
        IPage<Task> page3 = taskService.getTasksWithPagination(2, 2);
        assertEquals(1, page3.getRecords().size());
        assertEquals(2, page3.getCurrent());
    }

    @Test
    void canGetTasksWithPaginationAndStatusFilter() {
        // Create tasks with different statuses
        CreateTaskDTO dto1 = new CreateTaskDTO();
        dto1.setStrategyId(testStrategy.getId());
        dto1.setStrategyParams(new HashMap<>());
        Task task1 = taskService.createTask(dto1);

        CreateTaskDTO dto2 = new CreateTaskDTO();
        dto2.setStrategyId(testStrategy.getId());
        dto2.setStrategyParams(new HashMap<>());
        Task task2 = taskService.createTask(dto2);

        // Update statuses
        task1.setStatus("RUNNING");
        taskMapper.updateById(task1);

        task2.setStatus("STOPPED");
        taskMapper.updateById(task2);

        // Test filtering by RUNNING status
        IPage<Task> runningTasks = taskService.getTasksWithPagination(0, 10, "RUNNING");
        assertEquals(1, runningTasks.getRecords().size());
        assertEquals("RUNNING", runningTasks.getRecords().get(0).getStatus());

        // Test filtering by STOPPED status
        IPage<Task> stoppedTasks = taskService.getTasksWithPagination(0, 10, "STOPPED");
        assertEquals(1, stoppedTasks.getRecords().size());
        assertEquals("STOPPED", stoppedTasks.getRecords().get(0).getStatus());
    }

    @Test
    void canGetTasksWithFlexiblePagination() {
        // Create 3 tasks
        for (int i = 0; i < 3; i++) {
            CreateTaskDTO dto = new CreateTaskDTO();
            dto.setStrategyId(testStrategy.getId());
            dto.setStrategyParams(new HashMap<>());
            taskService.createTask(dto);
        }

        // Test with Page object
        Page<Task> pageRequest = new Page<>(0, 2);
        IPage<Task> result = taskService.getTasks(pageRequest, null);

        assertEquals(2, result.getRecords().size());
        assertEquals(3, result.getTotal());
    }

    @Test
    void canGetTaskStatistics() {
        // Create tasks with different statuses
        for (int i = 0; i < 3; i++) {
            CreateTaskDTO dto = new CreateTaskDTO();
            dto.setStrategyId(testStrategy.getId());
            dto.setStrategyParams(new HashMap<>());
            Task task = taskService.createTask(dto);

            // Set different statuses
            if (i == 0) {
                task.setStatus("RUNNING");
                taskMapper.updateById(task);
            } else if (i == 1) {
                task.setStatus("STOPPED");
                taskMapper.updateById(task);
            }
            // i == 2 stays PENDING
        }

        // Get statistics
        Map<String, Long> stats = taskService.getTaskStatistics();

        // Verify all status keys are present
        assertTrue(stats.containsKey("PENDING"));
        assertTrue(stats.containsKey("CREATED"));
        assertTrue(stats.containsKey("RUNNING"));
        assertTrue(stats.containsKey("STOPPED"));
        assertTrue(stats.containsKey("FAILED"));
        assertTrue(stats.containsKey("COMPLETED"));

        // Verify counts
        assertEquals(1L, stats.get("PENDING"));
        assertEquals(0L, stats.get("CREATED"));
        assertEquals(1L, stats.get("RUNNING"));
        assertEquals(1L, stats.get("STOPPED"));
        assertEquals(0L, stats.get("FAILED"));
        assertEquals(0L, stats.get("COMPLETED"));
    }

    @Test
    void canGetTaskStatisticsWithEmptyDatabase() {
        // Clean up all tasks
        taskMapper.delete(null);

        Map<String, Long> stats = taskService.getTaskStatistics();

        // All statuses should be present with count 0
        assertEquals(0L, stats.get("PENDING"));
        assertEquals(0L, stats.get("CREATED"));
        assertEquals(0L, stats.get("RUNNING"));
        assertEquals(0L, stats.get("STOPPED"));
        assertEquals(0L, stats.get("FAILED"));
        assertEquals(0L, stats.get("COMPLETED"));
    }
}
