package com.taskm.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.sdk.exception.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for sending event notifications to listeners.
 *
 * <p>This client provides a unified method to notify listeners about business events.
 * It uses fire-and-forget mode - events are sent without waiting for responses.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Initialize client
 * ListenerClient listener = new ListenerClient();
 *
 * // 下订单
 * Map<String, Object> placeOrderResult = listener.notify("place_order", Map.of(
 *     "symbol", "BTC/USDT",
 *     "price", 50000,
 *     "quantity", 0.1
 * ));
 *
 * // 撤销订单
 * Map<String, Object> cancelOrderResult = listener.notify("cancel_order", Map.of(
 *     "orderId", "ORD-12345"
 * ));
 *
 * // 检查订单
 * Map<String, Object> checkOrderResult = listener.notify("check_order", Map.of(
 *     "orderId", "ORD-12345"
 * ));
 * }</pre>
 *
 * <p>The listener container runs an HTTP server that receives these events.</p>
 */
public class ListenerClient {

    private static final String ENV_ENDPOINT = "LISTENER_ENDPOINT";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final String endpoint;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * Creates a new ListenerClient with the specified endpoint.
     *
     * @param endpoint the base URL of the listener instance API
     */
    public ListenerClient(String endpoint) {
        this(endpoint, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Creates a new ListenerClient with the specified endpoint and timeout.
     *
     * @param endpoint the base URL of the listener instance API
     * @param timeoutSeconds request timeout in seconds
     */
    public ListenerClient(String endpoint, int timeoutSeconds) {
        // Determine endpoint from parameter or environment
        String finalEndpoint;
        if (endpoint == null || endpoint.trim().isEmpty()) {
            String envEndpoint = Environment.get(ENV_ENDPOINT);
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
     * Default constructor that reads endpoint from LISTENER_ENDPOINT environment variable.
     */
    public ListenerClient() {
        this(null);
    }

    /**
     * Send a business event notification to the listener.
     *
     * <p>This is the unified method for all event types. The eventType parameter
     * determines which type of event is being sent.</p>
     *
     * <h3>Supported Event Types:</h3>
     * <ul>
     *   <li>place_order - 下订单事件</li>
     *   <li>cancel_order - 撤销订单事件</li>
     *   <li>check_order - 检查订单事件</li>
     * </ul>
     *
     * @param eventType the type of event (e.g., "place_order", "cancel_order", "check_order")
     * @param payload the event data as key-value pairs
     * @return the response data from the listener as key-value pairs
     * @throws ListenerConnectionException if connection to listener fails
     * @throws ListenerTimeoutException if request times out
     */
    public Map<String, Object> notify(String eventType, Map<String, Object> payload) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("eventType cannot be null or empty");
        }

        if (payload == null) {
            payload = new HashMap<>();
        }

        String url = endpoint + "/api/event/" + eventType;

        try {
            // Send request and wait for response
            String jsonResponse = httpClient.post(url, payload);

            // Parse response
            Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
            return response;

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
        } catch (Exception e) {
            throw new ListenerConnectionException(
                "Failed to parse response: " + e.getMessage(),
                url,
                e
            );
        }
    }

    /**
     * HTTP client wrapper using java.net.http.HttpClient.
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

                // Get response body as string
                return client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }
    }
}
