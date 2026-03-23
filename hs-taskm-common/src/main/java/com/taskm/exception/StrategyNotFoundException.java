package com.taskm.exception;

/**
 * Exception thrown when a strategy is not found.
 */
public class StrategyNotFoundException extends RuntimeException {

    public StrategyNotFoundException(String message) {
        super(message);
    }

    public StrategyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
