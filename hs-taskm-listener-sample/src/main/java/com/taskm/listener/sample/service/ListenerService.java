package com.taskm.listener.sample.service;

import com.taskm.sdk.Logger;
import com.taskm.listener.sample.dto.TaskCompletedRequest;
import com.taskm.listener.sample.dto.TaskFailedRequest;
import com.taskm.listener.sample.dto.TaskStartedRequest;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for processing task lifecycle events.
 *
 * <p>This service demonstrates how to handle task events. You can extend this class
 * to add custom logic like:</p>
 *
 * <ul>
 *   <li>Sending notifications (email, Slack, webhooks)</li>
 *   <li>Updating external systems</li>
 *   <li>Writing to databases</li>
 *   <li>Triggering other workflows</li>
 *   <li>Collecting metrics and analytics</li>
 * </ul>
 *
 * <h3>Example Extension:</h3>
 * <pre>{@code
 * @Service
 * public class CustomListenerService extends ListenerService {
 *
 *     private final EmailService emailService;
 *
 *     public CustomListenerService(Logger logger, EmailService emailService) {
 *         super(logger);
 *         this.emailService = emailService;
 *     }
 *
 *     @Override
 *     public void handleTaskStarted(TaskStartedRequest request) {
 *         super.handleTaskStarted(request);
 *         // Add custom logic
 *         emailService.sendNotification("Task " + request.getTaskId() + " started");
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Service
public class ListenerService {

    private final Logger logger;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

    /**
     * Create a new listener service.
     */
    public ListenerService() {
        // Use SDK logger for consistent logging
        this.logger = new Logger("ListenerService");
    }

    /**
     * Create a new listener service with custom logger.
     *
     * @param logger custom logger instance
     */
    public ListenerService(Logger logger) {
        this.logger = logger;
    }

    /**
     * Handle task-started event.
     *
     * <p>Override this method to add custom logic when a task starts.</p>
     *
     * @param request task started request
     */
    public void handleTaskStarted(TaskStartedRequest request) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("taskId", request.getTaskId());
        data.put("timestamp", request.getTimestamp() != null ?
            request.getTimestamp().atZone(java.time.ZoneId.systemDefault()).format(formatter) : null);
        data.put("message", request.getMessage());
        logger.info("Task started", data);

        // Add your custom logic here
        // Examples:
        // - Send notification
        // - Update database
        // - Call webhook
        // - Record metrics
    }

    /**
     * Handle task-completed event.
     *
     * <p>Override this method to add custom logic when a task completes successfully.</p>
     *
     * @param request task completed request
     */
    public void handleTaskCompleted(TaskCompletedRequest request) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("taskId", request.getTaskId());
        data.put("timestamp", request.getTimestamp() != null ?
            request.getTimestamp().atZone(java.time.ZoneId.systemDefault()).format(formatter) : null);
        data.put("message", request.getMessage());
        data.put("result", request.getResult());
        logger.info("Task completed", data);

        // Process task result
        if (request.getResult() != null) {
            logger.debug("Task result details", "result", formatResult(request.getResult()));
        }

        // Add your custom logic here
        // Examples:
        // - Extract profit/loss from result
        // - Update trading statistics
        // - Send success notification
        // - Archive results
    }

    /**
     * Handle task-failed event.
     *
     * <p>Override this method to add custom logic when a task fails.</p>
     *
     * @param request task failed request
     */
    public void handleTaskFailed(TaskFailedRequest request) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("taskId", request.getTaskId());
        data.put("timestamp", request.getTimestamp() != null ?
            request.getTimestamp().atZone(java.time.ZoneId.systemDefault()).format(formatter) : null);
        data.put("error", request.getError());
        logger.error("Task failed", data);

        if (request.getStackTrace() != null) {
            logger.debug("Task failure stack trace", "stackTrace", request.getStackTrace());
        }

        // Add your custom logic here
        // Examples:
        // - Send alert notification
        // - Log to error tracking system
        // - Trigger investigation workflow
        // - Update failure metrics
    }

    /**
     * Format result map for logging.
     *
     * @param result result map
     * @return formatted string
     */
    private String formatResult(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");
        result.forEach((key, value) -> {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(key).append("=").append(value);
        });
        sb.append("}");
        return sb.toString();
    }
}
