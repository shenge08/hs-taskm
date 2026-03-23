package com.taskm.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.sdk.exception.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for sending event notifications to listeners.
 *
 * <p>This client provides simple methods to notify listeners about task lifecycle events.
 * It uses fire-and-forget mode - events are sent without waiting for responses.
 *
 * <p>The listener container runs an HTTP server that receives these events.
 */
public class ListenerClient {

    private static final String ENV_ENDPOINT = "LISTENER_ENDPOINT";
    private static final String ENV_TASK_ID = "TASK_ID";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final String endpoint;
    private final Long taskId;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * Creates a new ListenerClient with the specified endpoint and task ID.
     *
     * @param endpoint the base URL of the listener instance API
     * @param taskId the task ID
     */
    public ListenerClient(String endpoint, Long taskId) {
        this(endpoint, taskId, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Creates a new ListenerClient with the specified endpoint, task ID, and timeout.
     *
     * @param endpoint the base URL of the listener instance API
     * @param taskId the task ID
     * @param timeoutSeconds request timeout in seconds
     */
    public ListenerClient(String endpoint, Long taskId, int timeoutSeconds) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            String envEndpoint = System.getenv(ENV_ENDPOINT);
            if (envEndpoint == null || envEndpoint.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Endpoint must be provided either as parameter or " +
                    "through " + ENV_ENDPOINT + " environment variable"
                );
            }
            this.endpoint = envEndpoint.trim();
        } else {
            this.endpoint = endpoint.trim();
        }

        // Remove trailing slash
        this.endpoint = this.endpoint.replaceAll("/$", "");

        if (taskId == null) {
            String envTaskId = System.getenv(ENV_TASK_ID);
            if (envTaskId == null || envTaskId.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "taskId must be provided either as parameter or " +
                    "through " + ENV_TASK_ID + " environment variable"
                );
            }
            try {
                this.taskId = Long.parseLong(envTaskId.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    ENV_TASK_ID + " environment variable must be a valid integer"
                );
            }
        } else {
            this.taskId = taskId;
        }

        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new HttpClient(Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * Notify the listener that the task has started.
     *
     * This sends an event notification without waiting for response.
     *
     * @throws ListenerConnectionException if connection to listener fails
     * @throws ListenerTimeoutException if request times out
     */
    public void onTaskStarted() {
        sendEvent("task-started", new HashMap<>());
    }

    /**
     * Notify the listener that the task has completed successfully.
     *
     * This sends an event notification without waiting for response.
     *
     * @param result task execution result
     * @throws ListenerConnectionException if connection to listener fails
     * @throws ListenerTimeoutException if request times out
     */
    public void onTaskCompleted(Map<String, Object> result) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("result", result);
        sendEvent("task-completed", extraData);
    }

    /**
     * Notify the listener that the task has failed.
     *
     * This sends an event notification without waiting for response.
     *
     * @param error error message describing the failure
     * @throws ListenerConnectionException if connection to listener fails
     * @throws ListenerTimeoutException if request times out
     */
    public void onTaskFailed(String error) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("error", error);
        sendEvent("task-failed", extraData);
    }

    /**
     * Internal method to send an event to the listener.
     *
     * This uses fire-and-forget mode - sends the event and immediately returns.
     */
    private void sendEvent(String eventType, Map<String, Object> extraData) {
        String url = endpoint + "/" + eventType;

        // Build request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("timestamp", Instant.now().toString());
        payload.putAll(extraData);

        try {
            // Fire and forget - don't wait for response
            httpClient.post(url, payload);
        } catch (IOException e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw new ListenerTimeoutException(
                    "Request timed out after " + timeoutSeconds + "s",
                    url,
                    timeoutSeconds
                );
            }
            throw new ListenerConnectionException(
                "Failed to send event: " + e.getMessage(),
                url,
                e
            );
        }
    }

    /**
     * Simple HTTP client wrapper using java.net.http.HttpClient.
     */
    private static class HttpClient {
        private final java.net.http.HttpClient client;
        private final Duration timeout;

        HttpClient(Duration timeout) {
            this.timeout = timeout;
            this.client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        }

        void post(String url, Object body) throws IOException {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonBody = mapper.writeValueAsString(body);

                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                // Discard response - fire and forget
                client.send(request, java.net.http.HttpResponse.BodyHandlers.discarding());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }
    }
}
