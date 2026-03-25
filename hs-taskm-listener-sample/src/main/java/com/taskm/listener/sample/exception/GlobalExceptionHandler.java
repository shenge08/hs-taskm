package com.taskm.listener.sample.exception;

import com.taskm.listener.sample.dto.ListenerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

/**
 * Global exception handler for REST controllers.
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle resource not found exceptions.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ListenerResponse> handleNotFound(NoResourceFoundException ex) {
        ListenerResponse response = ListenerResponse.error(
            "Resource not found: " + ex.getResourcePath()
        );
        response.setTimestamp(Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ListenerResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ListenerResponse response = ListenerResponse.error(
            "Invalid argument: " + ex.getMessage()
        );
        response.setTimestamp(Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ListenerResponse> handleGenericException(Exception ex) {
        ListenerResponse response = ListenerResponse.error(
            "Internal server error: " + ex.getMessage()
        );
        response.setTimestamp(Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
