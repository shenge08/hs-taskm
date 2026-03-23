package com.taskm.service;

import com.taskm.service.impl.LogServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LogService.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogServiceTest {

    @Mock
    private LogRouter logRouter;

    private LogService logService;

    private Path tempLogFile;
    private Long testTaskId = 1L;

    @BeforeEach
    void setUp() throws IOException {
        logService = new LogServiceImpl(logRouter);

        // Create temporary log file
        tempLogFile = Files.createTempFile("test-task-" + testTaskId, ".log");

        // Write test log content
        String logContent = """
            2024-01-01 10:00:00 INFO Task started
            2024-01-01 10:00:01 DEBUG Processing data
            2024-01-01 10:00:02 ERROR An error occurred
            2024-01-01 10:00:03 INFO Task completed
            2024-01-01 10:00:04 DEBUG Cleanup done
            """;

        Files.writeString(tempLogFile, logContent);

        // Configure mock router
        when(logRouter.getLogPath(testTaskId)).thenReturn(tempLogFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temp file
        if (tempLogFile != null && Files.exists(tempLogFile)) {
            Files.delete(tempLogFile);
        }
    }

    @Test
    void testGetTaskLogs_shouldReturnLogLinesWithOffsetAndLimit() {
        // When
        List<String> logs = logService.getTaskLogs(testTaskId, 1, 2);

        // Then
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).contains("Processing data"));
        assertTrue(logs.get(1).contains("An error occurred"));
    }

    @Test
    void testGetTaskLogs_shouldReturnAllLinesWhenNoLimit() {
        // When
        List<String> logs = logService.getTaskLogs(testTaskId, 0, 100);

        // Then
        assertEquals(5, logs.size());
        assertTrue(logs.get(0).contains("Task started"));
        assertTrue(logs.get(4).contains("Cleanup done"));
    }

    @Test
    void testGetTaskLogs_shouldReturnErrorMessageWhenFileNotFound() {
        // Given
        Long nonExistentTaskId = 999L;
        when(logRouter.getLogPath(nonExistentTaskId)).thenReturn("/non/existent/path.log");

        // When
        List<String> logs = logService.getTaskLogs(nonExistentTaskId, 0, 10);

        // Then
        assertEquals(1, logs.size());
        assertTrue(logs.get(0).contains("Log file not found"));
    }

    @Test
    void testGetLogTail_shouldReturnLastNLines() {
        // When
        List<String> tail = logService.getLogTail(testTaskId, 2);

        // Then
        assertEquals(2, tail.size());
        assertTrue(tail.get(0).contains("Task completed"));
        assertTrue(tail.get(1).contains("Cleanup done"));
    }

    @Test
    void testGetLogTail_shouldReturnAllLinesWhenRequestExceedsFile() {
        // When
        List<String> tail = logService.getLogTail(testTaskId, 100);

        // Then
        assertEquals(5, tail.size());
        assertTrue(tail.get(0).contains("Task started"));
    }

    @Test
    void testGetLogTail_shouldReturnErrorMessageWhenFileNotFound() {
        // Given
        Long nonExistentTaskId = 999L;
        when(logRouter.getLogPath(nonExistentTaskId)).thenReturn("/non/existent/path.log");

        // When
        List<String> tail = logService.getLogTail(nonExistentTaskId, 10);

        // Then
        assertEquals(1, tail.size());
        assertTrue(tail.get(0).contains("Log file not found"));
    }

    @Test
    void testSearchLogs_shouldReturnMatchingLines() {
        // When
        List<String> results = logService.searchLogs(testTaskId, "ERROR", 10);

        // Then
        assertEquals(1, results.size());
        assertTrue(results.get(0).contains("An error occurred"));
    }

    @Test
    void testSearchLogs_shouldReturnEmptyListWhenNoMatches() {
        // When
        List<String> results = logService.searchLogs(testTaskId, "NONEXISTENT", 10);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchLogs_shouldRespectLimit() {
        // When
        List<String> results = logService.searchLogs(testTaskId, "INFO", 1);

        // Then
        assertEquals(1, results.size());
        assertTrue(results.get(0).contains("Task started"));
    }

    @Test
    void testSearchLogs_shouldReturnEmptyListWhenFileNotFound() {
        // Given
        Long nonExistentTaskId = 999L;
        when(logRouter.getLogPath(nonExistentTaskId)).thenReturn("/non/existent/path.log");

        // When
        List<String> results = logService.searchLogs(nonExistentTaskId, "keyword", 10);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetLogMetadata_shouldReturnCorrectMetadata() {
        // When
        LogService.LogMetadata metadata = logService.getLogMetadata(testTaskId);

        // Then
        assertNotNull(metadata);
        assertEquals(tempLogFile.toString(), metadata.getFilePath());
        assertTrue(metadata.getFileSize() > 0);
        assertEquals(5, metadata.getLineCount());
        assertNotNull(metadata.getLastModified());
        assertNotEquals("N/A", metadata.getLastModified());
    }

    @Test
    void testGetLogMetadata_shouldReturnDefaultValuesWhenFileNotFound() {
        // Given
        Long nonExistentTaskId = 999L;
        when(logRouter.getLogPath(nonExistentTaskId)).thenReturn("/non/existent/path.log");

        // When
        LogService.LogMetadata metadata = logService.getLogMetadata(nonExistentTaskId);

        // Then
        assertNotNull(metadata);
        assertEquals("/non/existent/path.log", metadata.getFilePath());
        assertEquals(0, metadata.getFileSize());
        assertEquals(0, metadata.getLineCount());
        assertEquals("N/A", metadata.getLastModified());
    }

    @Test
    void testGetTaskLogs_shouldHandleEmptyFile() throws IOException {
        // Given
        Long emptyTaskId = 2L;
        Path emptyFile = Files.createTempFile("test-empty", ".log");
        when(logRouter.getLogPath(emptyTaskId)).thenReturn(emptyFile.toString());

        try {
            // When
            List<String> logs = logService.getTaskLogs(emptyTaskId, 0, 10);

            // Then
            assertTrue(logs.isEmpty());
        } finally {
            Files.delete(emptyFile);
        }
    }
}
