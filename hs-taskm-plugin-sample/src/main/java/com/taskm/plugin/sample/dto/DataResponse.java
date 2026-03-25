package com.taskm.plugin.sample.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 数据插件响应 DTO。
 *
 * <p>这是数据插件返回的统一响应格式，包含请求的数据和元信息。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse {

    /**
     * 请求是否成功。
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * 响应消息。
     */
    @JsonProperty("message")
    private String message;

    /**
     * 返回的数据。
     */
    @JsonProperty("data")
    private Map<String, Object> data;

    /**
     * 响应时间戳。
     */
    @JsonProperty("timestamp")
    private Instant timestamp;

    /**
     * 创建成功响应。
     *
     * @param data 返回的数据
     * @return 成功响应
     */
    public static DataResponse success(Map<String, Object> data) {
        return new DataResponse(true, "Data retrieved successfully", data, Instant.now());
    }

    /**
     * 创建成功响应（带消息）。
     *
     * @param data 返回的数据
     * @param message 响应消息
     * @return 成功响应
     */
    public static DataResponse success(Map<String, Object> data, String message) {
        return new DataResponse(true, message, data, Instant.now());
    }

    /**
     * 创建错误响应。
     *
     * @param message 错误消息
     * @return 错误响应
     */
    public static DataResponse error(String message) {
        return new DataResponse(false, message, null, Instant.now());
    }
}
