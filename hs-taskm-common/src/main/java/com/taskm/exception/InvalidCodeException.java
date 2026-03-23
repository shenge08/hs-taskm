package com.taskm.exception;

/**
 * Exception thrown when code snippet is invalid or empty.
 */
public class InvalidCodeException extends RuntimeException {

    public InvalidCodeException(String message) {
        super(message);
    }

    public InvalidCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
