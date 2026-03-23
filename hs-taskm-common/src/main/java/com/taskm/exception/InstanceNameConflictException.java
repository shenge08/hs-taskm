package com.taskm.exception;

/**
 * Exception thrown when an instance name conflicts with an existing instance.
 */
public class InstanceNameConflictException extends RuntimeException {

    public InstanceNameConflictException(String message) {
        super(message);
    }

    public InstanceNameConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
