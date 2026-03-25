package com.taskm.listener.sample.service;

import com.taskm.listener.sample.client.BondTradingApiClient;
import com.taskm.sdk.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for processing business events.
 *
 * <p>This service demonstrates how to handle business events. You can extend this class
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
 * <h3>Supported Event Types:</h3>
 * <ul>
 *   <li>place_order - 下订单事件</li>
 *   <li>cancel_order - 撤销订单事件</li>
 *   <li>check_order - 检查订单事件</li>
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
 *     public Map<String, Object> handlePlaceOrder(Map<String, Object> payload) {
 *         Map<String, Object> result = super.handlePlaceOrder(payload);
 *         // Add custom logic
 *         String orderId = (String) result.get("orderId");
 *         emailService.sendNotification("Order placed: " + orderId);
 *         return result;
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Service
public class ListenerService {

    private final Logger logger;
    private final BondTradingApiClient bondTradingClient;

    // Withdrawal polling configuration
    private static final int WITHDRAWAL_MAX_RETRIES =
        Integer.parseInt(getEnvOrDefault("WITHDRAWAL_MAX_RETRIES", "10"));
    private static final int WITHDRAWAL_POLL_INTERVAL_MS =
        Integer.parseInt(getEnvOrDefault("WITHDRAWAL_POLL_INTERVAL_MS", "1000"));
    private static final int WITHDRAWAL_SUCCESS_WAIT_MS =
        Integer.parseInt(getEnvOrDefault("WITHDRAWAL_SUCCESS_WAIT_MS", "10000"));

    /**
     * Create a new listener service.
     */
    public ListenerService() {
        // Use SDK logger for consistent logging
        this.logger = new Logger("ListenerService");
        this.bondTradingClient = new BondTradingApiClient();
    }

    /**
     * Create a new listener service with custom logger and API client.
     *
     * @param logger custom logger instance
     * @param bondTradingClient BIMS API client
     */
    public ListenerService(Logger logger, BondTradingApiClient bondTradingClient) {
        this.logger = logger;
        this.bondTradingClient = bondTradingClient;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

    /**
     * 统一事件处理方法
     *
     * <p>此方法根据事件类型分发到不同的处理逻辑。</p>
     *
     * @param eventType 事件类型
     * @param payload 事件数据
     * @return 处理结果
     * @throws IllegalArgumentException 如果事件类型不支持
     */
    public Map<String, Object> handleEvent(String eventType, Map<String, Object> payload) {
        logger.info("Handling event: " + eventType, "payload", payload);

        switch (eventType) {
            case "place_order":
                return handlePlaceOrder(payload);

            case "cancel_order":
                return handleCancelOrder(payload);

            case "check_order":
                return handleCheckOrder(payload);

            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }

    /**
     * 处理下订单事件
     *
     * <p>This method calls the BIMS API to:
     * 1. Login to obtain authentication token
     * 2. Generate a convertible bond order</p>
     *
     * @param payload 订单数据
     * @return 处理结果，包含 taskId 等信息
     */
    protected Map<String, Object> handlePlaceOrder(Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 记录日志
            logger.info("Place order event received", "payload", payload);

            // Call BIMS API to generate order
            BondTradingApiClient.GenerateOrderResponse response =
                bondTradingClient.generateOrder(payload);

            // 返回处理结果
            result.put("success", true);
            result.put("taskId", response.taskId());
            result.put("message", "订单已提交");
            result.put("timestamp", Instant.now().toEpochMilli());

            // 记录成功日志
            Map<String, Object> successLogData = new HashMap<>();
            successLogData.put("taskId", response.taskId());
            logger.info("Order placed successfully via BIMS API", successLogData);

        } catch (Exception e) {
            logger.error("Failed to place order via BIMS API", "error", e.getMessage());
            result.put("success", false);
            result.put("message", "订单提交失败: " + e.getMessage());
            result.put("timestamp", Instant.now().toEpochMilli());
        }

        return result;
    }

    /**
     * 处理撤销订单事件
     *
     * <p>This method calls the BIMS API to withdraw an order by task ID with polling logic:</p>
     * <ol>
     *   <li>Send withdrawal request</li>
     *   <li>Poll withdrawal result with configured interval and max retries</li>
     *   <li>If result is true (withdrawn successfully), wait configured time before returning</li>
     *   <li>If result is false (not withdrawn), check deal quantity to see if order was filled</li>
     * </ol>
     *
     * @param payload 订单数据，必须包含 taskId 和 subOrderId 字段
     * @return 处理结果
     */
    protected Map<String, Object> handleCancelOrder(Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 提取任务ID和子单ID
            String taskId = (String) payload.get("taskId");
            String subOrderId = (String) payload.get("subOrderId");

            if (taskId == null || taskId.trim().isEmpty()) {
                throw new IllegalArgumentException("taskId is required");
            }
            if (subOrderId == null || subOrderId.trim().isEmpty()) {
                subOrderId = "";
            }

            Map<String, Object> requestLogData = new HashMap<>();
            requestLogData.put("taskId", taskId);
            requestLogData.put("subOrderId", subOrderId);
            logger.info("Cancel order event received", requestLogData);

            // 1. Send withdrawal request
            BondTradingApiClient.WithdrawOrderResponse withdrawResponse =
                bondTradingClient.withdrawOrder(taskId);

            if (!withdrawResponse.result()) {
                result.put("success", false);
                result.put("taskId", taskId);
                result.put("message", withdrawResponse.promptMessage());
                result.put("timestamp", Instant.now().toEpochMilli());
                return result;
            }

            Map<String, Object> pollLogData = new HashMap<>();
            pollLogData.put("taskId", taskId);
            pollLogData.put("subOrderId", subOrderId);
            pollLogData.put("maxRetries", WITHDRAWAL_MAX_RETRIES);
            pollLogData.put("pollIntervalMs", WITHDRAWAL_POLL_INTERVAL_MS);
            logger.info("Withdrawal request accepted, starting to poll result", pollLogData);

            // 2. Poll withdrawal result
            for (int retry = 0; retry < WITHDRAWAL_MAX_RETRIES; retry++) {
                BondTradingApiClient.WithdrawalResultResponse resultResponse =
                    bondTradingClient.getWithdrawalResult(taskId, subOrderId);

                if (resultResponse.result()) {
                    // Withdrawal successful - wait for data sync
                    Map<String, Object> successLogData = new HashMap<>();
                    successLogData.put("taskId", taskId);
                    successLogData.put("waitMs", WITHDRAWAL_SUCCESS_WAIT_MS);
                    logger.info("Order withdrawn successfully, waiting for data sync", successLogData);

                    try {
                        Thread.sleep(WITHDRAWAL_SUCCESS_WAIT_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Map<String, Object> interruptLogData = new HashMap<>();
                        interruptLogData.put("taskId", taskId);
                        logger.warning("Wait interrupted during data sync", interruptLogData);
                    }

                    result.put("success", true);
                    result.put("taskId", taskId);
                    result.put("message", "订单已撤销");
                    result.put("timestamp", Instant.now().toEpochMilli());

                    logger.info("Order withdrawal completed", "taskId", taskId);
                    return result;
                }

                // result == false, check if order was filled
                BondTradingApiClient.GetDealQuantityResponse dealResponse =
                    bondTradingClient.getDealQuantity(taskId);

                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> resultList =
                    (java.util.List<Map<String, Object>>) dealResponse.data().get("result_data");

                if (resultList != null && !resultList.isEmpty()) {
                    double dealQuantity = 0.0;

                    if (subOrderId != null && !subOrderId.trim().isEmpty()) {
                        // subOrderId 不为空：找到匹配的子单，取其 deal_quantity
                        for (Map<String, Object> order : resultList) {
                            Object subOrderIdObj = order.get("sub_order_id");
                            if (subOrderIdObj != null && subOrderIdObj.toString().equals(subOrderId)) {
                                Object dealQuantityObj = order.get("deal_quantity");
                                if (dealQuantityObj != null) {
                                    dealQuantity = ((Number) dealQuantityObj).doubleValue();
                                }
                                break;
                            }
                        }
                    } else {
                        // subOrderId 为空：累加所有订单的 deal_quantity
                        for (Map<String, Object> order : resultList) {
                            Object dealQuantityObj = order.get("deal_quantity");
                            if (dealQuantityObj != null) {
                                dealQuantity += ((Number) dealQuantityObj).doubleValue();
                            }
                        }
                    }

                    if (dealQuantity > 0) {
                        // Order was filled during withdrawal
                        Map<String, Object> filledLogData = new HashMap<>();
                        filledLogData.put("taskId", taskId);
                        if (subOrderId != null && !subOrderId.trim().isEmpty()) {
                            filledLogData.put("subOrderId", subOrderId);
                        }
                        filledLogData.put("dealQuantity", dealQuantity);
                        logger.info("Order was filled during withdrawal", filledLogData);

                        result.put("success", true);
                        result.put("taskId", taskId);
                        result.put("message", "订单撤销过程中已成交");
                        result.put("dealQuantity", dealQuantity);
                        result.put("timestamp", Instant.now().toEpochMilli());

                        return result;
                    }
                }

                // Still pending, wait before next poll
                if (retry < WITHDRAWAL_MAX_RETRIES - 1) {
                    Map<String, Object> pendingLogData = new HashMap<>();
                    pendingLogData.put("taskId", taskId);
                    pendingLogData.put("attempt", retry + 1);
                    pendingLogData.put("maxAttempts", WITHDRAWAL_MAX_RETRIES);
                    logger.debug("Withdrawal still pending, polling again", pendingLogData);

                    try {
                        Thread.sleep(WITHDRAWAL_POLL_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Polling interrupted", e);
                    }
                }
            }

            // Max retries reached, still not confirmed
            Map<String, Object> timeoutLogData = new HashMap<>();
            timeoutLogData.put("taskId", taskId);
            timeoutLogData.put("maxRetries", WITHDRAWAL_MAX_RETRIES);
            logger.warning("Withdrawal result not confirmed after max retries", timeoutLogData);

            result.put("success", false);
            result.put("taskId", taskId);
            result.put("message", "撤单结果未确认，请稍后查询订单状态");
            result.put("timestamp", Instant.now().toEpochMilli());

        } catch (Exception e) {
            logger.error("Failed to withdraw order via BIMS API", "error", e.getMessage());
            result.put("success", false);
            result.put("message", "订单撤销失败: " + e.getMessage());
            result.put("timestamp", Instant.now().toEpochMilli());
        }

        return result;
    }

    /**
     * 处理检查订单事件
     *
     * <p>This method calls the BIMS API to get deal quantity by task ID.</p>
     *
     * @param payload 订单数据，必须包含 taskId 字段
     * @return 处理结果
     */
    protected Map<String, Object> handleCheckOrder(Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 提取任务ID
            String taskId = (String) payload.get("taskId");
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new IllegalArgumentException("taskId is required");
            }

            // 记录日志
            Map<String, Object> logData = new HashMap<>();
            logData.put("taskId", taskId);
            logger.info("Check order event received", logData);

            // Call BIMS API to get deal quantity
            BondTradingApiClient.GetDealQuantityResponse response =
                bondTradingClient.getDealQuantity(taskId);

            // 返回处理结果
            result.put("success", true);
            result.put("taskId", taskId);
            result.put("data", response.data());
            result.put("message", "订单状态查询成功");
            result.put("timestamp", Instant.now().toEpochMilli());

            // 记录成功日志
            logger.info("Order status retrieved successfully via BIMS API", "taskId", taskId);

        } catch (Exception e) {
            logger.error("Failed to get order status via BIMS API", "error", e.getMessage());
            result.put("success", false);
            result.put("message", "订单状态查询失败: " + e.getMessage());
            result.put("timestamp", Instant.now().toEpochMilli());
        }

        return result;
    }
}
