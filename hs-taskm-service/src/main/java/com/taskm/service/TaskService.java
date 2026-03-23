package com.taskm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taskm.dto.CreateTaskDTO;
import com.taskm.entity.Task;

import java.util.Map;

/**
 * Service interface for Task operations.
 */
public interface TaskService extends IService<Task> {

    /**
     * Create a new task.
     *
     * @param taskDTO task creation request
     * @return created task with ID
     */
    Task createTask(CreateTaskDTO taskDTO);

    /**
     * Get task by ID.
     *
     * @param id task ID
     * @return the task
     * @throws com.taskm.exception.TaskNotFoundException if task not found
     */
    Task getTask(Long id);

    /**
     * Get all tasks.
     *
     * @return list of all tasks
     */
    java.util.List<Task> getAllTasks();

    /**
     * Get tasks with pagination.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated tasks
     */
    IPage<Task> getTasksWithPagination(int page, int size);

    /**
     * Get tasks with pagination and filtering.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param status task status filter (optional)
     * @return paginated tasks
     */
    IPage<Task> getTasksWithPagination(int page, int size, String status);

    /**
     * Get tasks with pagination, filtering and sorting.
     *
     * @param pageRequest pagination request
     * @param status task status filter (optional)
     * @return paginated tasks
     */
    IPage<Task> getTasks(Page<Task> pageRequest, String status);

    /**
     * Get task statistics.
     * Returns count of tasks grouped by status.
     *
     * @return map of status to count
     */
    Map<String, Long> getTaskStatistics();
}
