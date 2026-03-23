package com.taskm.dto;

/**
 * Container status enumeration.
 */
public enum ContainerStatus {
    /**
     * Container has been created but not started.
     */
    CREATED,

    /**
     * Container is running.
     */
    RUNNING,

    /**
     * Container has been stopped.
     */
    STOPPED,

    /**
     * Container has exited with an error.
     */
    FAILED,

    /**
     * Container status is unknown.
     */
    UNKNOWN
}
