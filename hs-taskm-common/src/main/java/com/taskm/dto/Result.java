package com.taskm.dto;

import lombok.Data;

/**
 * Unified API response wrapper.
 * Provides consistent response format for all REST endpoints.
 *
 * @param <T> type of data being returned
 */
@Data
public class Result<T> {
    /**
     * Response code. 200 for success, 4xx/5xx for errors.
     */
    private Integer code;

    /**
     * Response message.
     */
    private String message;

    /**
     * Response data.
     */
    private T data;

    /**
     * Timestamp of response.
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Create a success response with data.
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "Success", data);
    }

    /**
     * Create a success response without data.
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "Success", null);
    }

    /**
     * Create a success response with custom message.
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * Create an error response.
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * Create a 404 error response.
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }

    /**
     * Create a 500 error response.
     */
    public static <T> Result<T> serverError(String message) {
        return new Result<>(500, message, null);
    }
}
