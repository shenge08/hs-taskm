package com.taskm.service.impl;

import com.taskm.dto.InjectionConfig;
import com.taskm.entity.DataPlugin;
import com.taskm.entity.Listener;
import com.taskm.entity.Task;
import com.taskm.exception.DataPluginNotFoundException;
import com.taskm.exception.InvalidCodeException;
import com.taskm.exception.InvalidParameterException;
import com.taskm.exception.ListenerNotFoundException;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.mapper.ListenerMapper;
import com.taskm.mapper.TaskMapper;
import com.taskm.service.CodeSnippetInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of code snippet injection.
 * Prepares container startup configuration by loading code and parameters from database.
 */
@Service
public class CodeSnippetInjectorImpl implements CodeSnippetInjector {

    private static final Logger logger = LoggerFactory.getLogger(CodeSnippetInjectorImpl.class);

    private final TaskMapper taskMapper;
    private final DataPluginMapper dataPluginMapper;
    private final ListenerMapper listenerMapper;

    @Value("${logs.base_dir:./logs}")
    private String logsBaseDir;

    @Autowired
    public CodeSnippetInjectorImpl(
            TaskMapper taskMapper,
            DataPluginMapper dataPluginMapper,
            ListenerMapper listenerMapper) {
        this.taskMapper = taskMapper;
        this.dataPluginMapper = dataPluginMapper;
        this.listenerMapper = listenerMapper;
    }

    /**
     * Set logs base directory (for testing).
     */
    public void setLogsBaseDir(String logsBaseDir) {
        this.logsBaseDir = logsBaseDir;
    }

    @Override
    public InjectionConfig prepareInjection(Long taskId) {
        logger.info("Preparing injection for task {}", taskId);

        // Load task from database
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found with id: " + taskId);
        }

        // Get instance endpoints from task
        String pluginEndpoint = task.getPluginEndpoint();
        String listenerEndpoint = task.getListenerEndpoint();

        // Get task parameters
        @SuppressWarnings("unchecked")
        Map<String, Object> taskParams = (Map<String, Object>) task.getParameters();

        Long pluginId = (Long) taskParams.get("pluginId");
        Long listenerId = (Long) taskParams.get("listenerId");
        @SuppressWarnings("unchecked")
        Map<String, Object> strategyParams = (Map<String, Object>) taskParams.get("strategyParams");

        // Load plugin if specified
        DataPlugin plugin = null;
        if (pluginId != null) {
            plugin = dataPluginMapper.selectById(pluginId);
            if (plugin == null) {
                throw new DataPluginNotFoundException("Plugin not found with id: " + pluginId);
            }
        }

        // Load listener if specified
        Listener listener = null;
        if (listenerId != null) {
            listener = listenerMapper.selectById(listenerId);
            if (listener == null) {
                throw new ListenerNotFoundException("Listener not found with id: " + listenerId);
            }
        }

        // Build injection config
        InjectionConfig config = new InjectionConfig();

        // Strategy params
        config.setStrategyParams(strategyParams);

        // Plugin code and params
        if (plugin != null) {
            if (plugin.getCode() == null || plugin.getCode().trim().isEmpty()) {
                throw new InvalidCodeException("Plugin code is empty for plugin: " + plugin.getName());
            }
            config.setPluginCode(encodeToBase64(plugin.getCode()));

            // Merge plugin parameters
            @SuppressWarnings("unchecked")
            Map<String, Object> userPluginParams = (Map<String, Object>) taskParams.get("pluginParams");
            Map<String, Object> mergedPluginParams = mergePluginParameters(plugin, userPluginParams);
            config.setPluginParams(mergedPluginParams);

            // Plugin metadata
            if (plugin.getMetadata() != null) {
                config.setPluginMetadata(plugin.getMetadata());
            }
        }

        // Listener code and params
        if (listener != null) {
            if (listener.getCode() == null || listener.getCode().trim().isEmpty()) {
                throw new InvalidCodeException("Listener code is empty for listener: " + listener.getName());
            }
            config.setListenerCode(encodeToBase64(listener.getCode()));

            // Merge listener parameters
            @SuppressWarnings("unchecked")
            Map<String, Object> userListenerParams = (Map<String, Object>) taskParams.get("listenerParams");
            Map<String, Object> mergedListenerParams = mergeListenerParameters(listener, userListenerParams);
            config.setListenerParams(mergedListenerParams);

            // Listener metadata
            if (listener.getMetadata() != null) {
                config.setListenerMetadata(listener.getMetadata());
            }
        }

        // Environment variables
        Map<String, String> envVars = buildEnvironmentVariables(taskId, config, pluginEndpoint, listenerEndpoint);
        config.setEnvironmentVariables(envVars);

        // Log path
        String logPath = generateLogPath(taskId);
        config.setLogPath(logPath);

        // Working directory
        config.setWorkingDirectory("/app");

        // Volume mounts (log directory)
        Map<String, String> volumes = new HashMap<>();
        volumes.put(getLogDirectoryPath(taskId), "/app/logs");
        volumes.put("/var/log/tasks", "/var/log/tasks");  // Mount for plugin/listener logs
        config.setVolumeMounts(volumes);

        logger.info("Prepared injection config for task {}", taskId);
        return config;
    }

    @Override
    public Map<String, String> getInjectionFiles(Long taskId) {
        InjectionConfig config = prepareInjection(taskId);

        Map<String, String> files = new HashMap<>();

        // Add plugin code if exists
        if (config.getPluginCode() != null) {
            files.put("plugin.py", config.getPluginCode());
        }

        // Add listener code if exists
        if (config.getListenerCode() != null) {
            files.put("listener.py", config.getListenerCode());
        }

        return files;
    }

    @Override
    public Map<String, String> getEnvironmentVariables(Long taskId) {
        InjectionConfig config = prepareInjection(taskId);
        return config.getEnvironmentVariables();
    }

    @Override
    public Map<String, String> getVolumeMounts(Long taskId) {
        InjectionConfig config = prepareInjection(taskId);
        return config.getVolumeMounts();
    }

    @Override
    public String generateLogPath(Long taskId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("task_%d_%s.log", taskId, timestamp);
    }

    @Override
    public void ensureLogDirectory(Long taskId) {
        try {
            Path logDir = Paths.get(getLogDirectoryPath(taskId));
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("Created log directory: {}", logDir);
            }
        } catch (Exception e) {
            logger.error("Failed to create log directory for task {}", taskId, e);
            throw new RuntimeException("Failed to create log directory", e);
        }
    }

    /**
     * Merge plugin parameters with user parameters.
     * User parameters override defaults.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mergePluginParameters(DataPlugin plugin, Map<String, Object> userParams) {
        Map<String, Object> merged = new HashMap<>();

        // Get default parameters from plugin's configParameters
        if (plugin.getConfigParameters() != null) {
            Map<String, Object> configParams = plugin.getConfigParameters();
            Object paramsObj = configParams.get("parameters");
            if (paramsObj instanceof List) {
                List<Map<String, Object>> paramDefs = (List<Map<String, Object>>) paramsObj;
                for (Map<String, Object> paramDef : paramDefs) {
                    String name = (String) paramDef.get("name");
                    Object defaultValue = paramDef.get("default");
                    if (defaultValue != null) {
                        merged.put(name, defaultValue);
                    }
                }
            }
        }

        // User parameters override defaults
        if (userParams != null) {
            merged.putAll(userParams);
        }

        return merged;
    }

    /**
     * Merge listener parameters with user parameters.
     * User parameters override defaults.
     */
    private Map<String, Object> mergeListenerParameters(Listener listener, Map<String, Object> userParams) {
        Map<String, Object> merged = new HashMap<>();

        // Get default parameters from listener
        if (listener.getParameterDefaults() != null) {
            merged.putAll(listener.getParameterDefaults());
        }

        // User parameters override defaults
        if (userParams != null) {
            merged.putAll(userParams);
        }

        return merged;
    }

    /**
     * Build environment variables for container.
     */
    private Map<String, String> buildEnvironmentVariables(Long taskId, InjectionConfig config,
                                                          String pluginEndpoint, String listenerEndpoint) {
        Map<String, String> env = new HashMap<>();

        // Plugin code and params
        if (config.getPluginCode() != null) {
            env.put("PLUGIN_CODE", config.getPluginCode());
        }
        if (config.getPluginParams() != null) {
            env.put("PLUGIN_PARAMS", toJson(config.getPluginParams()));
        }
        if (config.getPluginMetadata() != null) {
            env.put("PLUGIN_METADATA", toJson(config.getPluginMetadata()));
        }

        // Listener code and params
        if (config.getListenerCode() != null) {
            env.put("LISTENER_CODE", config.getListenerCode());
        }
        if (config.getListenerParams() != null) {
            env.put("LISTENER_PARAMS", toJson(config.getListenerParams()));
        }
        if (config.getListenerMetadata() != null) {
            env.put("LISTENER_METADATA", toJson(config.getListenerMetadata()));
        }

        // Strategy params
        if (config.getStrategyParams() != null) {
            env.put("STRATEGY_PARAMS", toJson(config.getStrategyParams()));
        }

        // Instance endpoints
        if (pluginEndpoint != null) {
            env.put("PLUGIN_ENDPOINT", pluginEndpoint);
        }
        if (listenerEndpoint != null) {
            env.put("LISTENER_ENDPOINT", listenerEndpoint);
        }

        // Task ID
        env.put("TASK_ID", String.valueOf(taskId));

        // Log path
        env.put("LOG_PATH", config.getLogPath());

        return env;
    }

    /**
     * Get log directory path for a task.
     */
    private String getLogDirectoryPath(Long taskId) {
        return Paths.get(logsBaseDir, "task-" + taskId).toString();
    }

    /**
     * Encode string to Base64.
     */
    private String encodeToBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    /**
     * Convert object to JSON string.
     */
    private String toJson(Object obj) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Failed to convert object to JSON", e);
            return "{}";
        }
    }
}
