package com.taskm.sdk.exception;

/**
 * Raised when plugin API returns an error response.
 */
public class PluginApiException extends PluginException {

    private final int statusCode;
    private final String endpoint;
    private final String responseBody;

    public PluginApiException(String message, int statusCode, String responseBody, String endpoint) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.endpoint = endpoint;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (statusCode > 0) {
            sb.append(" | status_code: ").append(statusCode);
        }
        if (endpoint != null) {
            sb.append(" | endpoint: ").append(endpoint);
        }
        if (responseBody != null) {
            sb.append(" | response: ").append(responseBody);
        }
        return sb.toString();
    }
}
