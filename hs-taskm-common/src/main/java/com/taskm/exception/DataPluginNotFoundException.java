package com.taskm.exception;

/**
 * Exception thrown when a data plugin is not found.
 */
public class DataPluginNotFoundException extends RuntimeException {

    public DataPluginNotFoundException(String message) {
        super(message);
    }

    public DataPluginNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
