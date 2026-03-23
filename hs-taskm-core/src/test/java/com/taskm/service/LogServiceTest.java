package com.taskm.service;

import com.taskm.service.impl.LogServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LogService.
 */
@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    private LogService logService;

    private Path tempLogFile;
    private Path tempTaskDir;
    private Long testTaskId = 1L;

    @BeforeEach
    void setUp() throws IOException {
        logService = new LogServiceImpl();

        // Create temporary task directory
        tempTaskDir = Files.createTempDirectory("task-" + testTaskId + "_");

        // Create temporary log file for strategy type
        tempLogFile = tempTaskDir.resolve("strategy.log");
        Files.createFile(tempLogFile);

        // Write test log content
        String logContent = """
            2024-01-01 10:00:00 INFO Task started
            2024-01-01 10:00:01 DEBUG Processing data
            2024-01-01 10:00:02 ERROR An error occurred
            2024-01-01 10:00:03 INFO Task completed
            2024-01-01 10:00:04 DEBUG Cleanup done
            """;

        Files.writeString(tempLogFile, logContent);

        // Set log directory path to temp directory
        System.setProperty("logs.base_dir", tempTaskDir.getParent().toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temp directory
        if (tempTaskDir != null && Files.exists(tempTaskDir)) {
            Files.walk(tempTaskDir)
                .sorted((a, b) -> b.compareTo(a))  // Reverse order to delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                });
        }
    }

    @Test
    void testGetTaskLogs_shouldReturnLogLinesWithOffsetAndLimit() {
        // When
        List<String> logs = logService.getTaskLogs(testTaskId, "strategy", 1, 2);

        // Then
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).contains("Processing data"));
        assertTrue(logs.get(1).contains("An error occurred"));
    }

    @Test
    void testGetTaskLogs_shouldReturnAllLinesWhenNoLimit() {
        // When
        List<String> logs = logService.getTaskLogs(testTaskId, "strategy", 0, 100);

        // Then
        assertEquals(5, logs.size());
        assertTrue(logs.get(0).contains("Task started"));
        assertTrue(logs.get(4).contains("Cleanup done"));
    }

    @Test
    void testGetTaskLogs_shouldReturnErrorMessageWhenFileNotFound() {
        // When
        List<String> logs = logService.getTaskLogs(999L, "strategy", 0, 10);

        // Then
        assertEquals(1, logs.size());
        assertTrue(logs.get(0).contains("Log file not found"));
    }

    @Test
    void testGetLogTail_shouldReturnLastNLines() {
        // When
        List<String> tail = logService.getLogTail(testTaskId, "strategy", 2);

        // Then
        assertEquals(2, tail.size());
        assertTrue(tail.get(0).contains("Task completed"));
        assertTrue(tail.get(1).contains("Cleanup done"));
    }

    @Test
    void testGetLogTail_shouldReturnAllLinesWhenRequestExceedsFile() {
        // When
        List<String> tail = logService.getLogTail(testTaskId, "strategy", 100);

        // Then
        assertEquals(5, tail.size());
        assertTrue(tail.get(0).contains("Task started"));
    }

    @Test
    void testGetLogTail_shouldReturnErrorMessageWhenFileNotFound() {
        // When
        List<String> tail = logService.getLogTail(999L, "strategy", 10);

        // Then
        assertEquals(1, tail.size());
        assertTrue(tail.get(0).contains("Log file not found"));
    }

    @Test
    void testSearchLogs_shouldReturnMatchingLines() {
        // When
        List<String> results = logService.searchLogs(testTaskId, "strategy", "ERROR", 10);

        // Then
        assertEquals(1, results.size());
        assertTrue(results.get(0).contains("An error occurred"));
    }

    @Test
    void testSearchLogs_shouldReturnEmptyListWhenNoMatches() {
        // When
        List<String> results = logService.searchLogs(testTaskId, "strategy", "NONEXISTENT", 10);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchLogs_shouldRespectLimit() {
        // When
        List<String> results = logService.searchLogs(testTaskId, "strategy", "INFO", 1);

        // Then
        assertEquals(1, results.size());
        assertTrue(results.get(0).contains("Task started"));
    }

    @Test
    void testSearchLogs_shouldReturnEmptyListWhenFileNotFound() {
        // When
        List<String> results = logService.searchLogs(999L, "strategy", "keyword", 10);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetLogMetadata_shouldReturnCorrectMetadata() {
        // When
        LogService.LogMetadata metadata = logService.getLogMetadata(testTaskId, "strategy");

        // Then
        assertNotNull(metadata);
        assertTrue(metadata.getFileSize() > 0);
        assertEquals(5, metadata.getLineCount());
        assertNotNull(metadata.getLastModified());
        assertNotEquals("N/A", metadata.getLastModified());
    }

    @Test
    void testGetLogMetadata_shouldReturnDefaultValuesWhenFileNotFound() {
        // When
        LogService.LogMetadata metadata = logService.getLogMetadata(999L, "strategy");

        // Then
        assertNotNull(metadata);
        assertEquals(0, metadata.getFileSize());
        assertEquals(0, metadata.getLineCount());
        assertEquals("N/A", metadata.getLastModified());
    }

    @Test
    void testGetTaskLogs_shouldHandleEmptyFile() throws IOException {
        // Given
        Path emptyFile = tempTaskDir.resolve("empty.log");
        Files.createFile(emptyFile);

        try {
            // Manually create a task directory for task 2
            Path task2Dir = tempTaskDir.getParent().resolve("task_2");
            Files.createDirectories(task2Dir);
            Path task2LogFile = task2Dir.resolve("strategy.log");
            Files.createFile(task2LogFile);

            // When
            List<String> logs = logService.getTaskLogs(2L, "strategy", 0, 10);

            // Then
            assertTrue(logs.isEmpty());
        } finally {
            Files.deleteIfExists(emptyFile);
        }
    }

    @Test
    void testGetAllLogMetadata_shouldReturnAllTypes() throws IOException {
        // Given - Create plugin and listener log files
        Path pluginLog = tempTaskDir.resolve("plugin.log");
        Path listenerLog = tempTaskDir.resolve("listener.log");
        Files.writeString(pluginLog, "Plugin log\n");
        Files.writeString(listenerLog, "Listener log\n");

        try {
            // When
            var metadataMap = logService.getAllLogMetadata(testTaskId);

            // Then
            assertNotNull(metadataMap);
            assertEquals(3, metadataMap.size());
            assertTrue(metadataMap.containsKey("strategy"));
            assertTrue(metadataMap.containsKey("plugin"));
            assertTrue(metadataMap.containsKey("listener"));
        } finally {
            Files.deleteIfExists(pluginLog);
            Files.deleteIfExists(listenerLog);
        }
    }
}
