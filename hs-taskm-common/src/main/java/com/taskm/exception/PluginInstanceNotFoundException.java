package com.taskm.exception;

/**
 * Exception thrown when a plugin instance is not found.
 */
public class PluginInstanceNotFoundException extends RuntimeException {

    public PluginInstanceNotFoundException(String message) {
        super(message);
    }

    public PluginInstanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
