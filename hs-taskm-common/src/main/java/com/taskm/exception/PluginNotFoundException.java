package com.taskm.exception;

/**
 * Exception thrown when a plugin is not found.
 */
public class PluginNotFoundException extends RuntimeException {

    public PluginNotFoundException(String message) {
        super(message);
    }

    public PluginNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
