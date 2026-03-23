package com.taskm.exception;

/**
 * Exception thrown when task status is invalid for an operation.
 */
public class InvalidTaskStatusException extends RuntimeException {

    public InvalidTaskStatusException(String message) {
        super(message);
    }

    public InvalidTaskStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
