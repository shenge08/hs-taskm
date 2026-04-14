package com.taskm.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.sdk.exception.*;
import com.taskm.sdk.model.ApiResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * Client for calling data plugin HTTP APIs.
 *
 * <p>This client provides methods to fetch data from plugin instances.
 * It automatically reads the endpoint from environment variable if not provided.
 */
public class DataPluginClient {

    private static final String ENV_ENDPOINT = "PLUGIN_ENDPOINT";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final String endpoint;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * Creates a new DataPluginClient with the specified endpoint.
     *
     * @param endpoint the base URL of the plugin instance API
     */
    public DataPluginClient(String endpoint) {
        this(endpoint, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Default constructor that reads endpoint from PLUGIN_ENDPOINT environment variable.
     */
    public DataPluginClient() {
        this(null);
    }

    /**
     * Creates a new DataPluginClient with the specified endpoint and timeout.
     *
     * @param endpoint the base URL of the plugin instance API
     * @param timeoutSeconds request timeout in seconds
     */
    public DataPluginClient(String endpoint, int timeoutSeconds) {
        // Determine endpoint from parameter or environment
        String finalEndpoint;
        if (endpoint == null || endpoint.trim().isEmpty()) {
            String envEndpoint = System.getenv(ENV_ENDPOINT);
            if (envEndpoint == null || envEndpoint.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Endpoint must be provided either as parameter or " +
                    "through " + ENV_ENDPOINT + " environment variable"
                );
            }
            finalEndpoint = envEndpoint.trim();
        } else {
            finalEndpoint = endpoint.trim();
        }

        // Remove trailing slash
        this.endpoint = finalEndpoint.replaceAll("/$", "");

        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new HttpClient(Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * Fetch data from the plugin.
     *
     * @param params arbitrary parameters to pass as request body
     * @return map containing the plugin's response data
     * @throws PluginConnectionException if connection to plugin fails
     * @throws PluginTimeoutException if request times out
     * @throws PluginApiException if plugin returns an error response
     */
    public Map<String, Object> getData(Map<String, Object> params) {
        String url = endpoint + "/get-data";

        try {
            String responseBody = httpClient.post(url, params);
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);

            if (response.isError()) {
                throw new PluginApiException(
                    response.getError() != null ? response.getError() : "Plugin returned error status",
                    0,
                    responseBody,
                    url
                );
            }

            if (response.getData() == null) {
                return Map.of();
            }

            return response.getData();

        } catch (IOException e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw new PluginTimeoutException(
                    "Request timed out after " + timeoutSeconds + "s",
                    url,
                    timeoutSeconds
                );
            }
            throw new PluginConnectionException(
                "Failed to connect to plugin: " + e.getMessage(),
                url,
                e
            );
        }
    }

    /**
     * Check if the plugin is healthy.
     *
     * @return map containing health status
     * @throws PluginConnectionException if connection to plugin fails
     * @throws PluginTimeoutException if request times out
     */
    public Map<String, Object> healthCheck() {
        String url = endpoint + "/health";

        try {
            String responseBody = httpClient.get(url);
            return objectMapper.readValue(responseBody, Map.class);

        } catch (IOException e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw new PluginTimeoutException(
                    "Health check timed out after " + timeoutSeconds + "s",
                    url,
                    timeoutSeconds
                );
            }
            throw new PluginConnectionException(
                "Failed to connect to plugin for health check: " + e.getMessage(),
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

        String get(String url) throws IOException {
            try {
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .timeout(timeout)
                    .GET()
                    .build();

                java.net.http.HttpResponse<String> response = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() >= 400) {
                    throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
                }

                return response.body();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }

        String post(String url, Object body) throws IOException {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonBody = mapper.writeValueAsString(body);

                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                java.net.http.HttpResponse<String> response = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() >= 400) {
                    throw new PluginApiException(
                        "HTTP " + response.statusCode() + ": " + response.body(),
                        response.statusCode(),
                        response.body(),
                        url
                    );
                }

                return response.body();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }
    }
}
