package com.taskm.dto;

import lombok.Data;

import java.util.List;

/**
 * Result of log query operation.
 * Contains log lines and metadata.
 */
@Data
public class LogResult {

    /**
     * Task ID.
     */
    private Long taskId;

    /**
     * Log file path.
     */
    private String filePath;

    /**
     * File size in bytes.
     */
    private Long fileSize;

    /**
     * Total number of lines in the log file.
     */
    private Long lineCount;

    /**
     * Last modified timestamp.
     */
    private String lastModified;

    /**
     * Log lines returned.
     */
    private List<String> lines;

    /**
     * Number of lines returned.
     */
    private Integer count;

    /**
     * Offset used for pagination.
     */
    private Integer offset;

    /**
     * Limit used for pagination.
     */
    private Integer limit;

    public static LogResult of(Long taskId, String filePath, long fileSize, long lineCount, String lastModified, List<String> lines) {
        LogResult result = new LogResult();
        result.setTaskId(taskId);
        result.setFilePath(filePath);
        result.setFileSize(fileSize);
        result.setLineCount(lineCount);
        result.setLastModified(lastModified);
        result.setLines(lines);
        result.setCount(lines.size());
        return result;
    }
}
