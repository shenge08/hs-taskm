package com.taskm.service;

import com.taskm.dto.InjectionConfig;
import com.taskm.entity.DataPlugin;
import com.taskm.entity.Listener;
import com.taskm.entity.Task;
import com.taskm.exception.DataPluginNotFoundException;
import com.taskm.exception.InvalidCodeException;
import com.taskm.exception.ListenerNotFoundException;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.mapper.ListenerMapper;
import com.taskm.mapper.TaskMapper;
import com.taskm.service.impl.CodeSnippetInjectorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for CodeSnippetInjector.
 * Tests code snippet injection and container preparation logic.
 */
@ExtendWith(MockitoExtension.class)
class CodeSnippetInjectorTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private DataPluginMapper dataPluginMapper;

    @Mock
    private ListenerMapper listenerMapper;

    private CodeSnippetInjector codeSnippetInjector;

    private Task testTask;
    private DataPlugin testPlugin;
    private Listener testListener;
    private static final String logsBaseDir = "./test-logs";

    @BeforeEach
    void setUp() {
        codeSnippetInjector = new CodeSnippetInjectorImpl(taskMapper, dataPluginMapper, listenerMapper);
        ((CodeSnippetInjectorImpl) codeSnippetInjector).setLogsBaseDir(logsBaseDir);

        // Create test plugin with default parameters
        testPlugin = new DataPlugin();
        testPlugin.setId(1L);
        testPlugin.setName("stock-quote-plugin");
        testPlugin.setDescription("Get stock quotes from Yahoo Finance");
        testPlugin.setPluginType("market_data");
        testPlugin.setLanguage("python");
        testPlugin.setCode("def get_stock_quote(symbol): return {'symbol': symbol, 'price': 100.0}");

        Map<String, Object> pluginParams = new HashMap<>();
        pluginParams.put("parameters", List.of(
            Map.of("name", "symbol", "type", "string", "default", "AAPL", "required", true),
            Map.of("name", "timeout", "type", "integer", "default", 30, "required", false)
        ));
        testPlugin.setConfigParameters(pluginParams);

        Map<String, Object> pluginMetadata = new HashMap<>();
        pluginMetadata.put("author", "John Doe");
        pluginMetadata.put("version", "1.0");
        testPlugin.setMetadata(pluginMetadata);

        // Create test listener with default parameters
        testListener = new Listener();
        testListener.setId(1L);
        testListener.setName("email-notifier");
        testListener.setDescription("Send email notifications on task completion");
        testListener.setEventType("TASK_COMPLETED");
        testListener.setLanguage("python");
        testListener.setCode("def send_email(message): print('Sending email:', message)");

        Map<String, Object> listenerParams = new HashMap<>();
        listenerParams.put("recipients", List.of("admin@example.com"));
        listenerParams.put("subject", "Task notification");
        testListener.setParameterDefaults(listenerParams);

        Map<String, Object> listenerMetadata = new HashMap<>();
        listenerMetadata.put("author", "Jane Smith");
        listenerMetadata.put("version", "2.0");
        testListener.setMetadata(listenerMetadata);
        testListener.setEnabled(true);
    }

    @Test
    void canPrepareInjectionWithPluginOnly() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("strategyParams", Map.of("symbol", "TSLA"));
        taskParams.put("pluginParams", Map.of("symbol", "TSLA", "timeout", 60));

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);

        // Act
        InjectionConfig config = codeSnippetInjector.prepareInjection(1L);

        // Assert
        assertNotNull(config);
        assertNotNull(config.getPluginCode());
        assertNotNull(config.getPluginParams());
        assertEquals("TSLA", config.getPluginParams().get("symbol"));
        assertEquals(60, config.getPluginParams().get("timeout"));
        assertNull(config.getListenerCode());
        assertNotNull(config.getEnvironmentVariables());
        assertTrue(config.getEnvironmentVariables().containsKey("PLUGIN_CODE"));
        assertTrue(config.getEnvironmentVariables().containsKey("PLUGIN_PARAMS"));
        assertTrue(config.getEnvironmentVariables().containsKey("PLUGIN_METADATA"));
        assertNotNull(config.getLogPath());
        assertEquals("/app", config.getWorkingDirectory());
    }

    @Test
    void canPrepareInjectionWithListenerOnly() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("listenerId", 1L);
        taskParams.put("strategyParams", Map.of("strategy", "test"));
        taskParams.put("listenerParams", Map.of("recipients", List.of("custom@example.com")));

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(listenerMapper.selectById(1L)).thenReturn(testListener);

        // Act
        InjectionConfig config = codeSnippetInjector.prepareInjection(1L);

        // Assert
        assertNotNull(config);
        assertNotNull(config.getListenerCode());
        assertNotNull(config.getListenerParams());
        assertEquals(List.of("custom@example.com"), config.getListenerParams().get("recipients"));
        assertEquals("Task notification", config.getListenerParams().get("subject"));
        assertNull(config.getPluginCode());
        assertNotNull(config.getEnvironmentVariables());
        assertTrue(config.getEnvironmentVariables().containsKey("LISTENER_CODE"));
        assertTrue(config.getEnvironmentVariables().containsKey("LISTENER_PARAMS"));
        assertTrue(config.getEnvironmentVariables().containsKey("LISTENER_METADATA"));
    }

    @Test
    void canPrepareInjectionWithPluginAndListener() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("listenerId", 1L);
        taskParams.put("strategyParams", Map.of("symbol", "AAPL"));
        taskParams.put("pluginParams", Map.of("symbol", "MSFT"));
        taskParams.put("listenerParams", Map.of("subject", "Custom subject"));

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);
        when(listenerMapper.selectById(1L)).thenReturn(testListener);

        // Act
        InjectionConfig config = codeSnippetInjector.prepareInjection(1L);

        // Assert
        assertNotNull(config);
        assertNotNull(config.getPluginCode());
        assertNotNull(config.getListenerCode());
        assertEquals("MSFT", config.getPluginParams().get("symbol"));
        assertEquals(30, config.getPluginParams().get("timeout"));  // default value
        assertEquals(List.of("admin@example.com"), config.getListenerParams().get("recipients"));  // default
        assertEquals("Custom subject", config.getListenerParams().get("subject"));  // user override
        assertTrue(config.getEnvironmentVariables().containsKey("PLUGIN_CODE"));
        assertTrue(config.getEnvironmentVariables().containsKey("LISTENER_CODE"));
        assertTrue(config.getEnvironmentVariables().containsKey("STRATEGY_PARAMS"));
    }

    @Test
    void shouldUseDefaultParametersWhenNoUserParamsProvided() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("strategyParams", Map.of("test", "value"));

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);

        // Act
        InjectionConfig config = codeSnippetInjector.prepareInjection(1L);

        // Assert
        assertNotNull(config.getPluginParams());
        assertEquals("AAPL", config.getPluginParams().get("symbol"));  // default value
        assertEquals(30, config.getPluginParams().get("timeout"));  // default value
    }

    @Test
    void shouldThrowExceptionWhenPluginNotFound() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 99999L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(99999L)).thenReturn(null);

        // Act & Assert
        assertThrows(DataPluginNotFoundException.class, () -> {
            codeSnippetInjector.prepareInjection(1L);
        });
    }

    @Test
    void shouldThrowExceptionWhenListenerNotFound() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("listenerId", 99999L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(listenerMapper.selectById(99999L)).thenReturn(null);

        // Act & Assert
        assertThrows(ListenerNotFoundException.class, () -> {
            codeSnippetInjector.prepareInjection(1L);
        });
    }

    @Test
    void shouldThrowExceptionWhenPluginCodeIsEmpty() {
        // Arrange
        testPlugin.setCode("");
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);

        // Act & Assert
        assertThrows(InvalidCodeException.class, () -> {
            codeSnippetInjector.prepareInjection(1L);
        });
    }

    @Test
    void shouldThrowExceptionWhenPluginCodeIsWhitespace() {
        // Arrange
        testPlugin.setCode("   \n\t  ");
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);

        // Act & Assert
        assertThrows(InvalidCodeException.class, () -> {
            codeSnippetInjector.prepareInjection(1L);
        });
    }

    @Test
    void shouldThrowExceptionWhenListenerCodeIsEmpty() {
        // Arrange
        testListener.setCode("");
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("listenerId", 1L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(listenerMapper.selectById(1L)).thenReturn(testListener);

        // Act & Assert
        assertThrows(InvalidCodeException.class, () -> {
            codeSnippetInjector.prepareInjection(1L);
        });
    }

    @Test
    void canGetInjectionFilesWithPlugin() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);

        // Act
        Map<String, String> files = codeSnippetInjector.getInjectionFiles(1L);

        // Assert
        assertNotNull(files);
        assertEquals(1, files.size());
        assertTrue(files.containsKey("plugin.py"));
        assertNotNull(files.get("plugin.py"));
    }

    @Test
    void canGetInjectionFilesWithListener() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("listenerId", 1L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(listenerMapper.selectById(1L)).thenReturn(testListener);

        // Act
        Map<String, String> files = codeSnippetInjector.getInjectionFiles(1L);

        // Assert
        assertNotNull(files);
        assertEquals(1, files.size());
        assertTrue(files.containsKey("listener.py"));
        assertNotNull(files.get("listener.py"));
    }

    @Test
    void canGetInjectionFilesWithBoth() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("listenerId", 1L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);
        when(listenerMapper.selectById(1L)).thenReturn(testListener);

        // Act
        Map<String, String> files = codeSnippetInjector.getInjectionFiles(1L);

        // Assert
        assertNotNull(files);
        assertEquals(2, files.size());
        assertTrue(files.containsKey("plugin.py"));
        assertTrue(files.containsKey("listener.py"));
    }

    @Test
    void canGetEnvironmentVariables() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("listenerId", 1L);
        taskParams.put("strategyParams", Map.of("test", "value"));

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);
        when(listenerMapper.selectById(1L)).thenReturn(testListener);

        // Act
        Map<String, String> envVars = codeSnippetInjector.getEnvironmentVariables(1L);

        // Assert
        assertNotNull(envVars);
        assertTrue(envVars.containsKey("PLUGIN_CODE"));
        assertTrue(envVars.containsKey("PLUGIN_PARAMS"));
        assertTrue(envVars.containsKey("PLUGIN_METADATA"));
        assertTrue(envVars.containsKey("LISTENER_CODE"));
        assertTrue(envVars.containsKey("LISTENER_PARAMS"));
        assertTrue(envVars.containsKey("LISTENER_METADATA"));
        assertTrue(envVars.containsKey("STRATEGY_PARAMS"));
        assertTrue(envVars.containsKey("LOG_PATH"));
    }

    @Test
    void canGetVolumeMounts() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);

        // Act
        Map<String, String> volumes = codeSnippetInjector.getVolumeMounts(1L);

        // Assert
        assertNotNull(volumes);
        assertEquals(1, volumes.size());
        assertTrue(volumes.containsKey(logsBaseDir + "/task-1"));
        assertEquals("/app/logs", volumes.get(logsBaseDir + "/task-1"));
    }

    @Test
    void canGenerateLogPath() {
        // Arrange
        Long taskId = 123L;

        // Act
        String logPath = codeSnippetInjector.generateLogPath(taskId);

        // Assert
        assertNotNull(logPath);
        assertTrue(logPath.startsWith("task_123_"));
        assertTrue(logPath.endsWith(".log"));
    }

    @Test
    void canEnsureLogDirectory() throws Exception {
        // Arrange
        Long taskId = 456L;
        Path logDir = Paths.get(logsBaseDir, "task-" + taskId);

        // Ensure directory doesn't exist
        if (Files.exists(logDir)) {
            Files.delete(logDir);
        }

        // Act
        codeSnippetInjector.ensureLogDirectory(taskId);

        // Assert
        assertTrue(Files.exists(logDir));

        // Cleanup
        Files.deleteIfExists(logDir);
    }

    @Test
    void canEncodePluginCodeToBase64() {
        // Arrange
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("pluginId", 1L);
        taskParams.put("strategyParams", Map.of());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setStrategyId(1L);
        testTask.setStatus("PENDING");
        testTask.setParameters(taskParams);

        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(dataPluginMapper.selectById(1L)).thenReturn(testPlugin);

        String originalCode = testPlugin.getCode();

        // Act
        InjectionConfig config = codeSnippetInjector.prepareInjection(1L);
        String pluginCode = config.getPluginCode();

        // Assert
        assertNotNull(pluginCode);
        assertDoesNotThrow(() -> {
            java.util.Base64.getDecoder().decode(pluginCode);
        });
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {
        // Arrange
        when(taskMapper.selectById(99999L)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            codeSnippetInjector.prepareInjection(99999L);
        });
    }
}
