package com.taskm.exception;

/**
 * Exception thrown when an instance is not found.
 */
public class InstanceNotFoundException extends RuntimeException {

    public InstanceNotFoundException(String message) {
        super(message);
    }

    public InstanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
