package com.taskm.exception;

/**
 * Exception thrown when no default instance is found for a plugin or listener.
 */
public class NoDefaultInstanceException extends RuntimeException {

    public NoDefaultInstanceException(String message) {
        super(message);
    }

    public NoDefaultInstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
