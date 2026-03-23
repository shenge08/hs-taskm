package com.taskm.sdk.exception;

/**
 * Base exception for listener-related errors.
 */
public class ListenerException extends TaskmSdkException {

    public ListenerException(String message) {
        super(message);
    }

    public ListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
