package com.taskm.exception;

/**
 * Exception thrown when a listener instance is not found.
 */
public class ListenerInstanceNotFoundException extends RuntimeException {

    public ListenerInstanceNotFoundException(String message) {
        super(message);
    }

    public ListenerInstanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
