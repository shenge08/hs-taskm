package com.taskm.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.taskm.service.DockerLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Docker log service using Docker Java Client.
 * Fetches logs directly from Docker containers.
 */
@Service
public class DockerLogServiceImpl implements DockerLogService {

    private static final Logger logger = LoggerFactory.getLogger(DockerLogServiceImpl.class);

    private final DockerClient dockerClient;

    @Autowired
    public DockerLogServiceImpl(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public List<String> getContainerLogs(String containerId, int offset, int limit) {
        return getContainerLogs(containerId, "all", 0).stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getLogTail(String containerId, int lines) {
        return getContainerLogs(containerId, "all", lines);
    }

    @Override
    public List<String> getContainerLogs(String containerId, String type, int tail) {
        try {
            LogContainerCmd cmd = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true);

            // Set log type filter
            if ("stdout".equalsIgnoreCase(type)) {
                cmd.withStdOut(true).withStdErr(false);
            } else if ("stderr".equalsIgnoreCase(type)) {
                cmd.withStdOut(false).withStdErr(true);
            }

            // Set tail (0 means all lines)
            if (tail > 0) {
                cmd.withTail(tail);
            } else {
                cmd.withTailAll(); // Get all logs
            }

            // Collect logs using LogContainerResultCallback
            List<String> logLines = new ArrayList<>();

            try {
                cmd.exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame frame) {
                        String logLine = new String(frame.getPayload());
                        logLines.add(logLine);
                    }
                }).awaitCompletion();
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for logs from container {}", containerId);
                Thread.currentThread().interrupt();
                return List.of("Error: Interrupted while fetching logs");
            }

            return logLines;

        } catch (Exception e) {
            logger.error("Failed to get logs for container {}: {}", containerId, e.getMessage());
            return List.of("Error fetching logs: " + e.getMessage());
        }
    }

    @Override
    public List<String> searchLogs(String containerId, String keyword, int limit) {
        List<String> allLogs = getContainerLogs(containerId, "all", 0);

        var matchingLogs = allLogs.stream()
                .filter(line -> line.contains(keyword));

        if (limit > 0) {
            matchingLogs = matchingLogs.limit(limit);
        }

        return matchingLogs.collect(Collectors.toList());
    }
}
