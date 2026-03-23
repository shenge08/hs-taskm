package com.taskm.exception;

import com.taskm.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers.
 * Provides consistent error responses across all endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle StrategyNotFoundException.
     * Returns 404 status code.
     */
    @ExceptionHandler(StrategyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleStrategyNotFound(StrategyNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    /**
     * Handle DataPluginNotFoundException.
     * Returns 404 status code.
     */
    @ExceptionHandler(DataPluginNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleDataPluginNotFound(DataPluginNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    /**
     * Handle ListenerNotFoundException.
     * Returns 404 status code.
     */
    @ExceptionHandler(ListenerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleListenerNotFound(ListenerNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    /**
     * Handle TaskNotFoundException.
     * Returns 404 status code.
     */
    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleTaskNotFound(TaskNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    /**
     * Handle IllegalArgumentException.
     * Returns 400 status code.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return Result.error(400, ex.getMessage());
    }

    /**
     * Handle all other exceptions.
     * Returns 500 status code.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGeneralException(Exception ex) {
        return Result.serverError("Internal server error: " + ex.getMessage());
    }
}
