package com.taskm.plugin.sample.exception;

import com.taskm.plugin.sample.dto.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

/**
 * 全局异常处理器。
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理资源未找到异常。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<DataResponse> handleNotFound(NoResourceFoundException ex) {
        DataResponse response = DataResponse.error(
            "Resource not found: " + ex.getResourcePath()
        );
        response.setTimestamp(Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理非法参数异常。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<DataResponse> handleIllegalArgument(IllegalArgumentException ex) {
        DataResponse response = DataResponse.error(
            "Invalid argument: " + ex.getMessage()
        );
        response.setTimestamp(Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理所有其他异常。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataResponse> handleGenericException(Exception ex) {
        DataResponse response = DataResponse.error(
            "Internal server error: " + ex.getMessage()
        );
        response.setTimestamp(Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
