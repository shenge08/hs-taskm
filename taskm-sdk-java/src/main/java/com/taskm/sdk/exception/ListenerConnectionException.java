package com.taskm.sdk.exception;

/**
 * Raised when connection to listener endpoint fails.
 */
public class ListenerConnectionException extends ListenerException {

    private final String endpoint;

    public ListenerConnectionException(String message, String endpoint) {
        super(message);
        this.endpoint = endpoint;
    }

    public ListenerConnectionException(String message, String endpoint, Throwable cause) {
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
