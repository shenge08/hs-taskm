package com.taskm.exception;

/**
 * Exception thrown when a listener is not found.
 */
public class ListenerNotFoundException extends RuntimeException {

    public ListenerNotFoundException(String message) {
        super(message);
    }

    public ListenerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
