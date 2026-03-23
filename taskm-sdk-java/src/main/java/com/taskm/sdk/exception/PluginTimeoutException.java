package com.taskm.sdk.exception;

/**
 * Raised when plugin request times out.
 */
public class PluginTimeoutException extends PluginException {

    private final String endpoint;
    private final int timeout;

    public PluginTimeoutException(String message, String endpoint, int timeout) {
        super(message);
        this.endpoint = endpoint;
        this.timeout = timeout;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public String getMessage() {
        return super.getMessage() +
               (endpoint != null && timeout > 0 ? " (endpoint: " + endpoint + ", timeout: " + timeout + "s)" : "");
    }
}
