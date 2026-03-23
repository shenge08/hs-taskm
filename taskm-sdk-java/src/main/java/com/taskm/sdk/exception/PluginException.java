package com.taskm.sdk.exception;

/**
 * Base exception for plugin-related errors.
 */
public class PluginException extends TaskmSdkException {

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
