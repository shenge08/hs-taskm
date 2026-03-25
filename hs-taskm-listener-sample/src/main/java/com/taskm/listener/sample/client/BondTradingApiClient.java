package com.taskm.listener.sample.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with the Bond Trading Management System (BIMS) API.
 *
 * <p>This client provides methods to:</p>
 * <ul>
 *   <li>Login and obtain authentication token</li>
 *   <li>Generate convertible bond orders</li>
 *   <li>Withdraw orders by task ID</li>
 *   <li>Get deal quantity by task ID</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Component
public class BondTradingApiClient {

    private static final Logger logger = LoggerFactory.getLogger(BondTradingApiClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String username;
    private final String password;

    private String cachedToken;
    private long tokenExpiryTime;

    /**
     * Create a new BIMS API client with configuration from environment variables.
     *
     * <p>Required environment variables:</p>
     * <ul>
     *   <li>BIMS_BASE_URL - API base URL (e.g., http://172.16.90.114:8081)</li>
     *   <li>BIMS_USERNAME - Login username</li>
     *   <li>BIMS_PASSWORD - Login password</li>
     * </ul>
     *
     * @throws IllegalArgumentException if any required environment variable is missing
     */
    public BondTradingApiClient() {
        this.baseUrl = getEnvOrThrow("BIMS_BASE_URL");
        this.username = getEnvOrThrow("BIMS_USERNAME");
        this.password = getEnvOrThrow("BIMS_PASSWORD");

        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

        this.objectMapper = new ObjectMapper();

        logger.info("BondTradingApiClient initialized with base URL: {}", baseUrl);
    }

    /**
     * Login to BIMS and obtain authentication token.
     *
     * @return LoginResponse containing the authentication token
     * @throws IOException if login fails
     */
    public LoginResponse login() throws IOException {
        String url = baseUrl + "/bims/api/user/login";

        Map<String, String> credentials = new HashMap<>();
        credentials.put("user_name", username);
        credentials.put("pass_word", password);

        logger.info("Attempting login to BIMS as user: {}", username);

        try {
            String jsonBody = objectMapper.writeValueAsString(credentials);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            logger.debug("Login response: {}", responseBody);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            Integer code = (Integer) responseMap.get("code");
            if (code == null || code != 200) {
                throw new IOException("Login failed with code: " + code);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            String token = (String) data.get("token");

            // Cache token
            this.cachedToken = token;
            this.tokenExpiryTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000); // 2 hours

            logger.info("Login successful, token cached");
            return new LoginResponse(token);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Login request interrupted", e);
        }
    }

    /**
     * Get cached token or login if not available/expired.
     *
     * @return authentication token
     * @throws IOException if login fails
     */
    public String getOrRefreshToken() throws IOException {
        if (cachedToken == null || System.currentTimeMillis() > tokenExpiryTime) {
            login();
        }
        return cachedToken;
    }

    /**
     * Generate a convertible bond order.
     *
     * @param orderParams order parameters
     * @return GenerateOrderResponse containing task ID
     * @throws IOException if API call fails
     */
    public GenerateOrderResponse generateOrder(Map<String, Object> orderParams) throws IOException {
        String url = baseUrl + "/bims/api/tradeQuantifyScheme/generateProcessByStrategyOrderParams";
        String token = getOrRefreshToken();

        logger.info("Generating bond order with params: {}", orderParams);

        try {
            String jsonBody = objectMapper.writeValueAsString(orderParams);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            logger.debug("Generate order response: {}", responseBody);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            Integer code = (Integer) responseMap.get("code");
            if (code == null || code != 200) {
                String message = (String) responseMap.get("message");
                throw new IOException("Generate order failed: " + message);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            String taskId = (String) data.get("task_id");

            logger.info("Bond order generated successfully, task ID: {}", taskId);
            return new GenerateOrderResponse(taskId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Generate order request interrupted", e);
        }
    }

    /**
     * Withdraw order by task ID.
     *
     * @param taskId the task ID to withdraw
     * @return WithdrawOrderResponse containing result
     * @throws IOException if API call fails
     */
    public WithdrawOrderResponse withdrawOrder(String taskId) throws IOException {
        String url = baseUrl + "/bims/api/tradeQuantifyScheme/withdrawOrderByTaskId/" + taskId;
        String token = getOrRefreshToken();

        logger.info("Withdrawing order for task ID: {}", taskId);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-Token", token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            logger.debug("Withdraw order response: {}", responseBody);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            Integer code = (Integer) responseMap.get("code");
            if (code == null || code != 200) {
                String message = (String) responseMap.get("message");
                throw new IOException("Withdraw order failed: " + message);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            Boolean result = (Boolean) data.get("result");
            String promptMessage = (String) data.get("prompt_message");

            logger.info("Order withdrawn successfully: {}", promptMessage);
            return new WithdrawOrderResponse(result, promptMessage);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Withdraw order request interrupted", e);
        }
    }

    /**
     * Get strategy withdrawal result.
     *
     * @param taskId the task ID
     * @param subOrderId the sub-order ID
     * @return WithdrawalResultResponse containing withdrawal result
     * @throws IOException if API call fails
     */
    public WithdrawalResultResponse getWithdrawalResult(String taskId, String subOrderId) throws IOException {
        String url = baseUrl + "/bims/api/tradeQuantifyScheme/getStrategyWithdrawalResult";
        String token = getOrRefreshToken();

        logger.info("Getting withdrawal result for task ID: {}, sub-order ID: {}", taskId, subOrderId);

        try {
            // Build request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("task_id", taskId);
            requestBody.put("sub_order_id", subOrderId);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            logger.debug("Get withdrawal result response: {}", responseBody);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            Integer code = (Integer) responseMap.get("code");
            if (code == null || code != 200) {
                String message = (String) responseMap.get("message");
                throw new IOException("Get withdrawal result failed: " + message);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            Boolean result = (Boolean) data.get("result");

            logger.info("Withdrawal result retrieved for task ID: {}, result: {}", taskId, result);
            return new WithdrawalResultResponse(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Get withdrawal result request interrupted", e);
        }
    }

    /**
     * Get deal quantity by task ID.
     *
     * @param taskId the task ID to query
     * @return GetDealQuantityResponse containing deal information
     * @throws IOException if API call fails
     */
    public GetDealQuantityResponse getDealQuantity(String taskId) throws IOException {
        String url = baseUrl + "/bims/api/tradeQuantifyScheme/getDealQuantityByTaskId/" + taskId;
        String token = getOrRefreshToken();

        logger.info("Getting deal quantity for task ID: {}", taskId);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-Token", token)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            logger.debug("Get deal quantity response: {}", responseBody);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            Integer code = (Integer) responseMap.get("code");
            if (code == null || code != 200) {
                String message = (String) responseMap.get("message");
                throw new IOException("Get deal quantity failed: " + message);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");

            logger.info("Deal quantity retrieved successfully for task ID: {}", taskId);
            return new GetDealQuantityResponse(data);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Get deal quantity request interrupted", e);
        }
    }

    private String getEnvOrThrow(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Required environment variable '" + key + "' is not set or is empty"
            );
        }
        return value.trim();
    }

    /**
     * Login response data.
     */
    public record LoginResponse(String token) {}

    /**
     * Generate order response data.
     */
    public record GenerateOrderResponse(String taskId) {}

    /**
     * Withdraw order response data.
     */
    public record WithdrawOrderResponse(boolean result, String promptMessage) {}

    /**
     * Get deal quantity response data.
     */
    public record GetDealQuantityResponse(Map<String, Object> data) {}

    /**
     * Withdrawal result response data.
     */
    public record WithdrawalResultResponse(Boolean result) {}
}
