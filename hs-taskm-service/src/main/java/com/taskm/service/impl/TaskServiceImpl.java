package com.taskm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskm.dto.CreateTaskDTO;
import com.taskm.entity.DataPlugin;
import com.taskm.entity.DataPluginInstance;
import com.taskm.entity.Listener;
import com.taskm.entity.ListenerInstance;
import com.taskm.entity.Strategy;
import com.taskm.entity.Task;
import com.taskm.exception.DataPluginNotFoundException;
import com.taskm.exception.InstanceNotFoundException;
import com.taskm.exception.InstanceNotRunningException;
import com.taskm.exception.ListenerNotFoundException;
import com.taskm.exception.NoDefaultInstanceException;
import com.taskm.exception.TaskNotFoundException;
import com.taskm.mapper.DataPluginInstanceMapper;
import com.taskm.mapper.ListenerInstanceMapper;
import com.taskm.mapper.TaskMapper;
import com.taskm.service.DataPluginService;
import com.taskm.service.ListenerInstanceService;
import com.taskm.service.ListenerService;
import com.taskm.service.PluginInstanceService;
import com.taskm.service.StrategyService;
import com.taskm.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for Task operations.
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private final StrategyService strategyService;
    private final DataPluginService dataPluginService;
    private final ListenerService listenerService;
    private final PluginInstanceService pluginInstanceService;
    private final ListenerInstanceService listenerInstanceService;
    private final DataPluginInstanceMapper dataPluginInstanceMapper;
    private final ListenerInstanceMapper listenerInstanceMapper;

    @Autowired
    public TaskServiceImpl(
            StrategyService strategyService,
            DataPluginService dataPluginService,
            ListenerService listenerService,
            PluginInstanceService pluginInstanceService,
            ListenerInstanceService listenerInstanceService,
            DataPluginInstanceMapper dataPluginInstanceMapper,
            ListenerInstanceMapper listenerInstanceMapper) {
        this.strategyService = strategyService;
        this.dataPluginService = dataPluginService;
        this.listenerService = listenerService;
        this.pluginInstanceService = pluginInstanceService;
        this.listenerInstanceService = listenerInstanceService;
        this.dataPluginInstanceMapper = dataPluginInstanceMapper;
        this.listenerInstanceMapper = listenerInstanceMapper;
    }

    @Override
    @Transactional
    public Task createTask(CreateTaskDTO taskDTO) {
        // Validate strategy exists
        Strategy strategy = strategyService.getStrategy(taskDTO.getStrategyId());

        // Resolve plugin instance
        DataPluginInstance pluginInstance = null;
        DataPlugin plugin = null;

        if (taskDTO.getPluginInstanceId() != null) {
            // User specified instance ID
            pluginInstance = dataPluginInstanceMapper.selectById(taskDTO.getPluginInstanceId());
            if (pluginInstance == null) {
                throw new InstanceNotFoundException("Plugin instance not found with id: " + taskDTO.getPluginInstanceId());
            }

            // Validate instance status
            if (!"RUNNING".equals(pluginInstance.getStatus())) {
                throw new InstanceNotRunningException(
                    "Plugin instance is not running: " + pluginInstance.getName() + " (status: " + pluginInstance.getStatus() + ")"
                );
            }

            // Get plugin for language matching
            plugin = dataPluginService.getPlugin(pluginInstance.getPluginId());

        } else if (taskDTO.getPluginId() != null) {
            // User specified plugin ID, find default instance
            plugin = dataPluginService.getPlugin(taskDTO.getPluginId());

            // Language matching check
            if (!strategy.getLanguage().equals(plugin.getLanguage())) {
                throw new IllegalArgumentException(
                    "Language mismatch: strategy is " + strategy.getLanguage() +
                    " but plugin is " + plugin.getLanguage()
                );
            }

            // Find default instance
            pluginInstance = pluginInstanceService.getDefaultInstance(taskDTO.getPluginId());
            if (pluginInstance == null) {
                throw new NoDefaultInstanceException(
                    "No default instance found for plugin " + taskDTO.getPluginId() + ". Please specify an instance ID or set a default instance."
                );
            }

            // Validate default instance status
            if (!"RUNNING".equals(pluginInstance.getStatus())) {
                throw new InstanceNotRunningException(
                    "Default plugin instance is not running: " + pluginInstance.getName() + " (status: " + pluginInstance.getStatus() + ")"
                );
            }
        }

        // Resolve listener instance
        ListenerInstance listenerInstance = null;
        Listener listener = null;

        if (taskDTO.getListenerInstanceId() != null) {
            // User specified instance ID
            listenerInstance = listenerInstanceMapper.selectById(taskDTO.getListenerInstanceId());
            if (listenerInstance == null) {
                throw new InstanceNotFoundException("Listener instance not found with id: " + taskDTO.getListenerInstanceId());
            }

            // Validate instance status
            if (!"RUNNING".equals(listenerInstance.getStatus())) {
                throw new InstanceNotRunningException(
                    "Listener instance is not running: " + listenerInstance.getName() + " (status: " + listenerInstance.getStatus() + ")"
                );
            }

            // Get listener for language matching
            listener = listenerService.getListener(listenerInstance.getListenerId());

        } else if (taskDTO.getListenerId() != null) {
            // User specified listener ID, find default instance
            listener = listenerService.getListener(taskDTO.getListenerId());

            // Language matching check
            if (!strategy.getLanguage().equals(listener.getLanguage())) {
                throw new IllegalArgumentException(
                    "Language mismatch: strategy is " + strategy.getLanguage() +
                    " but listener is " + listener.getLanguage()
                );
            }

            // Find default instance
            listenerInstance = listenerInstanceService.getDefaultInstance(taskDTO.getListenerId());
            if (listenerInstance == null) {
                throw new NoDefaultInstanceException(
                    "No default instance found for listener " + taskDTO.getListenerId() + ". Please specify an instance ID or set a default instance."
                );
            }

            // Validate default instance status
            if (!"RUNNING".equals(listenerInstance.getStatus())) {
                throw new InstanceNotRunningException(
                    "Default listener instance is not running: " + listenerInstance.getName() + " (status: " + listenerInstance.getStatus() + ")"
                );
            }
        }

        // Build task parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("strategyId", taskDTO.getStrategyId());
        parameters.put("strategyParams", taskDTO.getStrategyParams() != null ? taskDTO.getStrategyParams() : new HashMap<>());

        if (plugin != null) {
            parameters.put("pluginId", plugin.getId());
            parameters.put("pluginParams", taskDTO.getPluginParams() != null ? taskDTO.getPluginParams() : new HashMap<>());
        }

        if (listener != null) {
            parameters.put("listenerId", listener.getId());
            parameters.put("listenerParams", taskDTO.getListenerParams() != null ? taskDTO.getListenerParams() : new HashMap<>());
        }

        // Create task entity
        Task task = new Task();
        task.setStrategyId(taskDTO.getStrategyId());
        task.setStatus("PENDING");
        task.setParameters(parameters);

        // Set instance IDs and endpoints
        if (pluginInstance != null) {
            task.setPluginInstanceId(pluginInstance.getId());
            task.setPluginEndpoint(buildPluginEndpoint(pluginInstance));
        }

        if (listenerInstance != null) {
            task.setListenerInstanceId(listenerInstance.getId());
            task.setListenerEndpoint(buildListenerEndpoint(listenerInstance));
        }

        // Save to database
        save(task);

        return task;
    }

    /**
     * Build plugin endpoint URL.
     * Format: http://plugin-{pluginId}:8080/instances/{instanceName}/api
     */
    private String buildPluginEndpoint(DataPluginInstance instance) {
        return String.format("http://plugin-%d:8080/instances/%s/api",
            instance.getPluginId(),
            instance.getName()
        );
    }

    /**
     * Build listener endpoint URL.
     * Format: http://listener-{listenerId}:8080/instances/{instanceName}/api
     */
    private String buildListenerEndpoint(ListenerInstance instance) {
        return String.format("http://listener-%d:8080/instances/%s/api",
            instance.getListenerId(),
            instance.getName()
        );
    }

    @Override
    public Task getTask(Long id) {
        Task task = getById(id);
        if (task == null) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        return list();
    }

    @Override
    public IPage<Task> getTasksWithPagination(int page, int size) {
        Page<Task> pageRequest = new Page<>(page, size);
        pageRequest.addOrder(OrderItem.desc("created_at"));
        return page(pageRequest);
    }

    @Override
    public IPage<Task> getTasksWithPagination(int page, int size, String status) {
        Page<Task> pageRequest = new Page<>(page, size);
        pageRequest.addOrder(OrderItem.desc("created_at"));

        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Task::getStatus, status);
        }

        return page(pageRequest, queryWrapper);
    }

    @Override
    public IPage<Task> getTasks(Page<Task> pageRequest, String status) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Task::getStatus, status);
        }
        return page(pageRequest, queryWrapper);
    }

    @Override
    public Map<String, Long> getTaskStatistics() {
        List<Task> allTasks = list();

        Map<String, Long> statistics = allTasks.stream()
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        // Ensure all statuses are present
        statistics.putIfAbsent("PENDING", 0L);
        statistics.putIfAbsent("CREATED", 0L);
        statistics.putIfAbsent("RUNNING", 0L);
        statistics.putIfAbsent("STOPPED", 0L);
        statistics.putIfAbsent("FAILED", 0L);
        statistics.putIfAbsent("COMPLETED", 0L);

        return statistics;
    }
}
