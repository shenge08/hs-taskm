package com.taskm.service.impl;

import com.taskm.service.LogRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of log routing and management.
 * Manages log file paths and directories for tasks.
 */
@Service
public class LogRouterImpl implements LogRouter {

    private static final Logger logger = LoggerFactory.getLogger(LogRouterImpl.class);

    @Value("${logs.base_dir:./logs}")
    private String logsBaseDir;

    @Override
    public String getLogPath(Long taskId) {
        // Get the most recent log file for the task
        List<File> logFiles = listLogFiles(taskId);

        if (logFiles.isEmpty()) {
            // If no log files exist, generate a new path
            String fileName = generateLogFileName(taskId);
            return Paths.get(getLogDirectoryPath(taskId), fileName).toString();
        }

        // Return the most recent log file
        logFiles.sort(Comparator.comparingLong(File::lastModified).reversed());
        return logFiles.get(0).getAbsolutePath();
    }

    @Override
    public String getLogDirectoryPath(Long taskId) {
        return Paths.get(logsBaseDir, "task-" + taskId).toString();
    }

    @Override
    public void ensureLogDirectory(Long taskId) {
        try {
            Path logDir = Paths.get(getLogDirectoryPath(taskId));
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("Created log directory: {}", logDir);
            }
        } catch (Exception e) {
            logger.error("Failed to create log directory for task {}", taskId, e);
            throw new RuntimeException("Failed to create log directory", e);
        }
    }

    @Override
    public String generateLogFileName(Long taskId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("task_%d_%s.log", taskId, timestamp);
    }

    @Override
    public List<File> listLogFiles(Long taskId) {
        String logDirPath = getLogDirectoryPath(taskId);
        File logDir = new File(logDirPath);

        if (!logDir.exists() || !logDir.isDirectory()) {
            return List.of();
        }

        File[] files = logDir.listFiles((dir, name) -> name.startsWith("task_" + taskId + "_") && name.endsWith(".log"));

        if (files == null) {
            return List.of();
        }

        return Arrays.stream(files)
            .filter(File::isFile)
            .collect(Collectors.toList());
    }

    @Override
    public boolean logFileExists(Long taskId) {
        String logPath = getLogPath(taskId);
        File logFile = new File(logPath);
        return logFile.exists() && logFile.isFile();
    }
}
