package com.taskm.exception;

/**
 * Exception thrown when attempting to delete an instance that is in use.
 */
public class InstanceInUseException extends RuntimeException {

    public InstanceInUseException(String message) {
        super(message);
    }

    public InstanceInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
