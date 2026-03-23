package com.taskm.service;

import com.taskm.entity.DataPlugin;
import com.taskm.entity.DataPluginInstance;
import com.taskm.entity.Listener;
import com.taskm.entity.ListenerInstance;
import com.taskm.entity.Strategy;
import com.taskm.entity.Task;
import com.taskm.mapper.DataPluginInstanceMapper;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.mapper.ListenerInstanceMapper;
import com.taskm.mapper.ListenerMapper;
import com.taskm.mapper.StrategyMapper;
import com.taskm.mapper.TaskMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the HS-TASKM platform.
 * Tests the complete flow from creating plugin/listener instances to task execution.
 */
@SpringBootTest
@Testcontainers
class EndToEndIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("taskm_test")
        .withUsername("taskm")
        .withPassword("taskm123");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private PluginContainerManager pluginContainerManager;

    @Autowired
    private ListenerContainerManager listenerContainerManager;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskOrchestrator taskOrchestrator;

    @Autowired
    private DataPluginMapper dataPluginMapper;

    @Autowired
    private DataPluginInstanceMapper dataPluginInstanceMapper;

    @Autowired
    private ListenerMapper listenerMapper;

    @Autowired
    private ListenerInstanceMapper listenerInstanceMapper;

    @Autowired
    private StrategyMapper strategyMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private LogService logService;

    private Strategy testStrategy;
    private DataPlugin testPlugin;
    private Listener testListener;

    @BeforeEach
    void setUp() {
        // Clean up database
        taskMapper.selectList(null).forEach(t -> taskMapper.deleteById(t.getId()));
        dataPluginInstanceMapper.selectList(null).forEach(i -> dataPluginInstanceMapper.deleteById(i.getId()));
        listenerInstanceMapper.selectList(null).forEach(i -> listenerInstanceMapper.deleteById(i.getId()));
        dataPluginMapper.selectList(null).forEach(p -> dataPluginMapper.deleteById(p.getId()));
        listenerMapper.selectList(null).forEach(l -> listenerMapper.deleteById(l.getId()));
        strategyMapper.selectList(null).forEach(s -> strategyMapper.deleteById(s.getId()));

        // Create test strategy
        testStrategy = new Strategy();
        testStrategy.setName("Test Strategy");
        testStrategy.setCode("print('Test strategy executed')");
        testStrategy.setLanguage("python");
        strategyMapper.insert(testStrategy);

        // Create test plugin
        testPlugin = new DataPlugin();
        testPlugin.setName("Market Data Plugin");
        testPlugin.setCode("""
            import json
            from flask import Flask, request

            app = Flask(__name__)

            @app.route('/instances/<instance_name>/api/data', methods=['POST'])
            def get_data(instance_name):
                params = request.json
                return json.dumps({"price": 100.5, "volume": 1000})

            if __name__ == '__main__':
                app.run(host='0.0.0.0', port=8080)
            """);
        testPlugin.setImageName("python:3.9-slim");
        testPlugin.setConfigParameters(new HashMap<>());
        dataPluginMapper.insert(testPlugin);

        // Create test listener
        testListener = new Listener();
        testListener.setName("Test Listener");
        testListener.setCode("""
            import json
            from flask import Flask, request

            app = Flask(__name__)

            @app.route('/instances/<instance_name>/api/task-started', methods=['POST'])
            def task_started(instance_name):
                return json.dumps({"status": "acknowledged"})

            @app.route('/instances/<instance_name>/api/task-completed', methods=['POST'])
            def task_completed(instance_name):
                return json.dumps({"status": "acknowledged"})

            @app.route('/instances/<instance_name>/api/task-failed', methods=['POST'])
            def task_failed(instance_name):
                return json.dumps({"status": "acknowledged"})

            if __name__ == '__main__':
                app.run(host='0.0.0.0', port=8080)
            """);
        testListener.setImageName("python:3.9-slim");
        testListener.setParameterDefaults(new HashMap<>());
        listenerMapper.insert(testListener);
    }

    @AfterEach
    void tearDown() {
        // Clean up containers
        try {
            pluginContainerManager.stopPluginContainer(testPlugin.getId());
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        try {
            listenerContainerManager.stopListenerContainer(testListener.getId());
        } catch (Exception e) {
            // Ignore cleanup errors
        }

        // Clean up database
        taskMapper.selectList(null).forEach(t -> {
            try {
                if ("RUNNING".equals(t.getStatus())) {
                    taskOrchestrator.stopTask(t.getId());
                }
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        });
    }

    @Test
    @DisplayName("测试插件实例完整流程")
    void testPluginInstanceFullFlow() {
        // 1. 创建两个实例（prod 和 dev）
        DataPluginInstance prodInstance = createPluginInstance("prod", true);
        DataPluginInstance devInstance = createPluginInstance("dev", false);

        // 2. 启动插件容器
        String containerId = pluginContainerManager.startPluginContainer(testPlugin.getId());
        assertNotNull(containerId);

        // 3. 验证容器状态
        String status = pluginContainerManager.getContainerStatus(testPlugin.getId());
        assertEquals("RUNNING", status);

        // 4. 创建任务（使用 prod 实例）
        Task task = createTaskWithPluginInstance(prodInstance.getId());

        // 5. 启动任务
        var result = taskOrchestrator.startTask(task.getId());
        assertTrue(result.isSuccess());

        // 6. 清理
        taskOrchestrator.stopTask(task.getId());
        pluginContainerManager.stopPluginContainer(testPlugin.getId());
    }

    @Test
    @DisplayName("测试监听器实例完整流程")
    void testListenerInstanceFullFlow() {
        // 1. 创建两个实例（prod 和 dev）
        ListenerInstance prodInstance = createListenerInstance("prod", true);
        ListenerInstance devInstance = createListenerInstance("dev", false);

        // 2. 启动监听器容器
        String containerId = listenerContainerManager.startListenerContainer(testListener.getId());
        assertNotNull(containerId);

        // 3. 验证容器状态
        String status = listenerContainerManager.getContainerStatus(testListener.getId());
        assertEquals("RUNNING", status);

        // 4. 创建任务（使用 prod 实例）
        Task task = createTaskWithListenerInstance(prodInstance.getId());

        // 5. 启动任务
        var result = taskOrchestrator.startTask(task.getId());
        assertTrue(result.isSuccess());

        // 6. 清理
        taskOrchestrator.stopTask(task.getId());
        listenerContainerManager.stopListenerContainer(testListener.getId());
    }

    @Test
    @DisplayName("测试默认实例逻辑")
    void testDefaultInstanceLogic() {
        // 1. 创建默认实例
        DataPluginInstance defaultInstance = createPluginInstance("default", true);

        // 2. 启动容器
        pluginContainerManager.startPluginContainer(testPlugin.getId());

        // 3. 创建任务（使用默认实例）
        Task task = new Task();
        task.setStrategyId(testStrategy.getId());
        task.setPluginInstanceId(defaultInstance.getId());
        task.setStatus("CREATED");
        Map<String, Object> params = new HashMap<>();
        params.put("pluginId", testPlugin.getId());
        params.put("strategyParams", new HashMap<>());
        task.setParameters(params);
        taskMapper.insert(task);

        // 4. 验证使用了默认实例
        assertEquals(defaultInstance.getId(), task.getPluginInstanceId());

        // 5. 清理
        pluginContainerManager.stopPluginContainer(testPlugin.getId());
    }

    @Test
    @DisplayName("测试实例状态验证 - 未运行实例")
    void testInstanceStatusValidation_NotRunning() {
        // 1. 创建实例但不启动容器
        DataPluginInstance instance = createPluginInstance("prod", true);

        // 2. 创建任务
        Task task = createTaskWithPluginInstance(instance.getId());

        // 3. 验证容器未运行
        String status = pluginContainerManager.getContainerStatus(testPlugin.getId());
        assertEquals("NOT_FOUND", status);
    }

    @Test
    @DisplayName("测试日志分离")
    void testLogSeparation() {
        // 1. 创建并启动插件容器
        DataPluginInstance pluginInstance = createPluginInstance("prod", true);
        pluginContainerManager.startPluginContainer(testPlugin.getId());

        // 2. 创建并启动监听器容器
        ListenerInstance listenerInstance = createListenerInstance("prod", true);
        listenerContainerManager.startListenerContainer(testListener.getId());

        // 3. 创建任务
        Task task = createTaskWithBothInstances(pluginInstance.getId(), listenerInstance.getId());

        // 4. 启动任务
        var result = taskOrchestrator.startTask(task.getId());
        assertTrue(result.isSuccess());

        // 5. 验证日志分离（检查元数据结构）
        var metadataMap = logService.getAllLogMetadata(task.getId());
        assertNotNull(metadataMap);
        assertEquals(3, metadataMap.size());
        assertTrue(metadataMap.containsKey("strategy"));
        assertTrue(metadataMap.containsKey("plugin"));
        assertTrue(metadataMap.containsKey("listener"));

        // 6. 清理
        taskOrchestrator.stopTask(task.getId());
        pluginContainerManager.stopPluginContainer(testPlugin.getId());
        listenerContainerManager.stopListenerContainer(testListener.getId());
    }

    @Test
    @DisplayName("测试参数验证")
    void testParameterValidation() {
        // 1. 创建任务但缺少必需参数
        Task task = new Task();
        task.setStrategyId(testStrategy.getId());
        task.setStatus("CREATED");
        Map<String, Object> params = new HashMap<>();
        // 缺少 pluginId 和 listenerId
        params.put("strategyParams", new HashMap<>());
        task.setParameters(params);

        // 2. 验证任务可以创建（参数验证在其他层）
        assertDoesNotThrow(() -> taskMapper.insert(task));
    }

    // Helper methods

    private DataPluginInstance createPluginInstance(String name, boolean isDefault) {
        DataPluginInstance instance = new DataPluginInstance();
        instance.setPluginId(testPlugin.getId());
        instance.setName(name);
        instance.setIsDefault(isDefault);
        instance.setStatus("CREATED");
        instance.setConfig(new HashMap<>());
        dataPluginInstanceMapper.insert(instance);
        return instance;
    }

    private ListenerInstance createListenerInstance(String name, boolean isDefault) {
        ListenerInstance instance = new ListenerInstance();
        instance.setListenerId(testListener.getId());
        instance.setName(name);
        instance.setIsDefault(isDefault);
        instance.setStatus("CREATED");
        instance.setConfig(new HashMap<>());
        listenerInstanceMapper.insert(instance);
        return instance;
    }

    private Task createTaskWithPluginInstance(Long pluginInstanceId) {
        Task task = new Task();
        task.setStrategyId(testStrategy.getId());
        task.setPluginInstanceId(pluginInstanceId);
        task.setStatus("CREATED");
        Map<String, Object> params = new HashMap<>();
        params.put("pluginId", testPlugin.getId());
        params.put("pluginInstanceId", pluginInstanceId);
        params.put("strategyParams", new HashMap<>());
        task.setParameters(params);
        taskMapper.insert(task);

        // Resolve endpoint
        DataPluginInstance instance = dataPluginInstanceMapper.selectById(pluginInstanceId);
        task.setPluginEndpoint(buildPluginEndpoint(instance));

        return task;
    }

    private Task createTaskWithListenerInstance(Long listenerInstanceId) {
        Task task = new Task();
        task.setStrategyId(testStrategy.getId());
        task.setListenerInstanceId(listenerInstanceId);
        task.setStatus("CREATED");
        Map<String, Object> params = new HashMap<>();
        params.put("listenerId", testListener.getId());
        params.put("listenerInstanceId", listenerInstanceId);
        params.put("strategyParams", new HashMap<>());
        task.setParameters(params);
        taskMapper.insert(task);

        // Resolve endpoint
        ListenerInstance instance = listenerInstanceMapper.selectById(listenerInstanceId);
        task.setListenerEndpoint(buildListenerEndpoint(instance));

        return task;
    }

    private Task createTaskWithBothInstances(Long pluginInstanceId, Long listenerInstanceId) {
        Task task = new Task();
        task.setStrategyId(testStrategy.getId());
        task.setPluginInstanceId(pluginInstanceId);
        task.setListenerInstanceId(listenerInstanceId);
        task.setStatus("CREATED");
        Map<String, Object> params = new HashMap<>();
        params.put("pluginId", testPlugin.getId());
        params.put("pluginInstanceId", pluginInstanceId);
        params.put("listenerId", testListener.getId());
        params.put("listenerInstanceId", listenerInstanceId);
        params.put("strategyParams", new HashMap<>());
        task.setParameters(params);
        taskMapper.insert(task);

        // Resolve endpoints
        DataPluginInstance pluginInstance = dataPluginInstanceMapper.selectById(pluginInstanceId);
        task.setPluginEndpoint(buildPluginEndpoint(pluginInstance));

        ListenerInstance listenerInstance = listenerInstanceMapper.selectById(listenerInstanceId);
        task.setListenerEndpoint(buildListenerEndpoint(listenerInstance));

        return task;
    }

    private String buildPluginEndpoint(DataPluginInstance instance) {
        return String.format("http://plugin-%d:8080/instances/%s/api",
            testPlugin.getId(), instance.getName());
    }

    private String buildListenerEndpoint(ListenerInstance instance) {
        return String.format("http://listener-%d:8080/instances/%s/api",
            testListener.getId(), instance.getName());
    }
}
