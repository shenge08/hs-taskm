package com.taskm.listener.sample.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard response DTO for all listener endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListenerResponse {

    /**
     * Whether the request was processed successfully.
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * Response message.
     */
    @JsonProperty("message")
    private String message;

    /**
     * Timestamp of the response.
     */
    @JsonProperty("timestamp")
    private Instant timestamp;

    /**
     * Create a success response.
     */
    public static ListenerResponse success(String message) {
        return new ListenerResponse(true, message, Instant.now());
    }

    /**
     * Create an error response.
     */
    public static ListenerResponse error(String message) {
        return new ListenerResponse(false, message, Instant.now());
    }
}
