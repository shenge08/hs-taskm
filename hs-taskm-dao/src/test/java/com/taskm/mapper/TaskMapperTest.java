package com.taskm.mapper;

import com.taskm.entity.Strategy;
import com.taskm.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TaskMapper.
 * Tests CRUD operations for tasks.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private StrategyMapper strategyMapper;

    private Task testTask;
    private Strategy testStrategy;

    @BeforeEach
    void setUp() {
        // Clean up tasks first (due to foreign key constraints)
        taskMapper.delete(null);
        strategyMapper.delete(null);

        // Create test strategy first (required by foreign key constraint)
        testStrategy = new Strategy();
        testStrategy.setName("test-strategy-" + UUID.randomUUID().toString());
        testStrategy.setDescription("Test strategy for task creation");
        testStrategy.setLanguage("python");
        testStrategy.setCode("def strategy(): pass");
        strategyMapper.insert(testStrategy);

        // Verify strategy was created
        assertNotNull(testStrategy.getId(), "Strategy ID should not be null after insert");

        // Create test data
        testTask = new Task();
        testTask.setStrategyId(testStrategy.getId());
        testTask.setStatus("PENDING");

        Map<String, Object> params = new HashMap<>();
        params.put("symbol", "AAPL");
        params.put("interval", "1h");
        testTask.setParameters(params);

        testTask.setErrorMessage(null);
    }

    @Test
    void canInsertTask() {
        int result = taskMapper.insert(testTask);
        assertEquals(1, result);
        assertNotNull(testTask.getId());
        assertNotNull(testTask.getCreatedAt());
    }

    @Test
    void canSelectById() {
        taskMapper.insert(testTask);
        Task found = taskMapper.selectById(testTask.getId());
        assertNotNull(found);
        assertEquals(testStrategy.getId(), found.getStrategyId());
        assertEquals("PENDING", found.getStatus());
    }

    @Test
    void canSelectAll() {
        taskMapper.insert(testTask);

        Task task2 = new Task();
        task2.setStrategyId(testStrategy.getId());
        task2.setStatus("PENDING");
        taskMapper.insert(task2);

        assertEquals(2, taskMapper.selectList(null).size());
    }

    @Test
    void canUpdateTask() {
        taskMapper.insert(testTask);
        testTask.setStatus("RUNNING");
        testTask.setStartedAt(LocalDateTime.now());

        int result = taskMapper.updateById(testTask);
        assertEquals(1, result);

        Task updated = taskMapper.selectById(testTask.getId());
        assertEquals("RUNNING", updated.getStatus());
        assertNotNull(updated.getStartedAt());
    }

    @Test
    void canUpdateTaskResult() {
        taskMapper.insert(testTask);
        testTask.setStatus("COMPLETED");
        testTask.setCompletedAt(LocalDateTime.now());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("signals", 5);
        testTask.setResult(result);

        int updateResult = taskMapper.updateById(testTask);
        assertEquals(1, updateResult);

        Task updated = taskMapper.selectById(testTask.getId());
        assertEquals("COMPLETED", updated.getStatus());
        assertNotNull(updated.getResult());
        assertEquals(5, updated.getResult().get("signals"));
    }

    @Test
    void canDeleteTask() {
        taskMapper.insert(testTask);
        Long id = testTask.getId();

        int result = taskMapper.deleteById(id);
        assertEquals(1, result);

        Task deleted = taskMapper.selectById(id);
        assertNull(deleted);
    }
}
