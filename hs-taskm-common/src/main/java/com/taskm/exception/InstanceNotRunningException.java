package com.taskm.exception;

/**
 * Exception thrown when attempting to use an instance that is not in RUNNING status.
 */
public class InstanceNotRunningException extends RuntimeException {

    public InstanceNotRunningException(String message) {
        super(message);
    }

    public InstanceNotRunningException(String message, Throwable cause) {
        super(message, cause);
    }
}
