package com.taskm.sdk.exception;

/**
 * Base exception for all TaskM SDK exceptions.
 */
public class TaskmSdkException extends RuntimeException {

    public TaskmSdkException(String message) {
        super(message);
    }

    public TaskmSdkException(String message, Throwable cause) {
        super(message, cause);
    }
}
