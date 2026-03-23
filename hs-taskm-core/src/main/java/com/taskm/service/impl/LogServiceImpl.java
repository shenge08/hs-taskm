package com.taskm.service.impl;

import com.taskm.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of log reading and searching.
 * Provides methods to read and search task log files.
 *
 * Log directory structure: /var/log/taskm/tasks/task_{id}/{strategy,plugin,listener}.log
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String BASE_LOG_DIR = "/var/log/taskm/tasks";
    private static final List<String> LOG_TYPES = List.of("strategy", "plugin", "listener");

    /**
     * Get log file path for a specific task and container type.
     */
    private String getLogPath(Long taskId, String type) {
        return String.format("%s/task_%d/%s.log", BASE_LOG_DIR, taskId, type);
    }

    /**
     * Get task directory path for a specific task.
     */
    private Path getTaskDirectory(Long taskId) {
        return Paths.get(String.format("%s/task_%d", BASE_LOG_DIR, taskId));
    }

    @Override
    public List<String> getTaskLogs(Long taskId, String type, int offset, int limit) {
        String logPath = getLogPath(taskId, type);
        File logFile = new File(logPath);

        if (!logFile.exists()) {
            logger.warn("Log file not found for task {} type {}: {}", taskId, type, logPath);
            return List.of("Log file not found for task " + taskId + " type " + type);
        }

        try {
            return Files.lines(Paths.get(logPath))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to read log file for task {} type {}", taskId, type, e);
            return List.of("Error reading log file: " + e.getMessage());
        }
    }

    @Override
    public List<String> getLogTail(Long taskId, String type, int lines) {
        String logPath = getLogPath(taskId, type);
        File logFile = new File(logPath);

        if (!logFile.exists()) {
            logger.warn("Log file not found for task {} type {}: {}", taskId, type, logPath);
            return List.of("Log file not found for task " + taskId + " type " + type);
        }

        try {
            // Read all lines and get the last N
            List<String> allLines = Files.readAllLines(Paths.get(logPath));
            int startIndex = Math.max(0, allLines.size() - lines);
            return allLines.subList(startIndex, allLines.size());
        } catch (IOException e) {
            logger.error("Failed to read log file for task {} type {}", taskId, type, e);
            return List.of("Error reading log file: " + e.getMessage());
        }
    }

    @Override
    public List<String> searchLogs(Long taskId, String type, String keyword, int limit) {
        String logPath = getLogPath(taskId, type);
        File logFile = new File(logPath);

        if (!logFile.exists()) {
            logger.warn("Log file not found for task {} type {}: {}", taskId, type, logPath);
            return List.of();
        }

        try {
            var stream = Files.lines(Paths.get(logPath))
                .filter(line -> line.contains(keyword));

            if (limit > 0) {
                stream = stream.limit(limit);
            }

            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to search log file for task {} type {}", taskId, type, e);
            return List.of("Error searching log file: " + e.getMessage());
        }
    }

    @Override
    public Map<String, List<String>> getAllTaskLogs(Long taskId) {
        Map<String, List<String>> allLogs = new HashMap<>();

        for (String type : LOG_TYPES) {
            String logPath = getLogPath(taskId, type);
            File logFile = new File(logPath);

            if (logFile.exists()) {
                try {
                    List<String> lines = Files.readAllLines(Paths.get(logPath));
                    allLogs.put(type, lines);
                } catch (IOException e) {
                    logger.error("Failed to read log file for task {} type {}", taskId, type, e);
                    allLogs.put(type, List.of("Error reading log file: " + e.getMessage()));
                }
            } else {
                allLogs.put(type, List.of());
            }
        }

        return allLogs;
    }

    @Override
    public LogMetadata getLogMetadata(Long taskId, String type) {
        String logPath = getLogPath(taskId, type);
        File logFile = new File(logPath);

        if (!logFile.exists()) {
            return new LogMetadata(logPath, 0, 0, "N/A");
        }

        try {
            long fileSize = logFile.length();

            // Count lines efficiently
            long lineCount = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                while (reader.readLine() != null) {
                    lineCount++;
                }
            }

            // Get last modified time
            FileTime fileTime = Files.getLastModifiedTime(Paths.get(logPath));
            String lastModified = Instant.ofEpochMilli(fileTime.toMillis())
                .atZone(ZoneId.systemDefault())
                .format(TIMESTAMP_FORMATTER);

            return new LogMetadata(logPath, fileSize, lineCount, lastModified);

        } catch (IOException e) {
            logger.error("Failed to get log metadata for task {} type {}", taskId, type, e);
            return new LogMetadata(logPath, 0, 0, "Error: " + e.getMessage());
        }
    }

    @Override
    public Map<String, LogMetadata> getAllLogMetadata(Long taskId) {
        Map<String, LogMetadata> metadataMap = new HashMap<>();

        for (String type : LOG_TYPES) {
            metadataMap.put(type, getLogMetadata(taskId, type));
        }

        return metadataMap;
    }

    @Override
    public void ensureLogDirectory(Long taskId) {
        Path taskDir = getTaskDirectory(taskId);

        try {
            if (!Files.exists(taskDir)) {
                Files.createDirectories(taskDir);
                logger.info("Created log directory for task {}: {}", taskId, taskDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create log directory for task {}: {}", taskId, taskDir, e);
            throw new RuntimeException("Failed to create log directory for task " + taskId, e);
        }
    }
}
