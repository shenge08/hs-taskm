package com.taskm.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Configuration for creating a Docker container.
 */
@Data
public class ContainerConfig {

    /**
     * Environment variables (e.g., {"KEY=value"}).
     */
    private List<String> env;

    /**
     * Volume bindings (e.g., ["/host/path:/container/path"]).
     */
    private List<String> binds;

    /**
     * Port bindings (e.g., {"8080/tcp": {}}).
     */
    private Map<String, Map<String, String>> portBindings;

    /**
     * Working directory inside the container.
     */
    private String workingDir;

    /**
     * Command to run (overrides image CMD).
     */
    private List<String> cmd;

    /**
     * Container name.
     */
    private String name;
}
