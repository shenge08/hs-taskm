package com.taskm.sdk.exception;

/**
 * Raised when connection to plugin endpoint fails.
 */
public class PluginConnectionException extends PluginException {

    private final String endpoint;

    public PluginConnectionException(String message, String endpoint) {
        super(message);
        this.endpoint = endpoint;
    }

    public PluginConnectionException(String message, String endpoint, Throwable cause) {
        super(message, cause);
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + (endpoint != null ? " (endpoint: " + endpoint + ")" : "");
    }
}
