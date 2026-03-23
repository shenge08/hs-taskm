package com.taskm.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for log reading and searching.
 * Provides methods to read and search task log files.
 */
public interface LogService {

    /**
     * Get task logs for a specific container type with pagination.
     *
     * @param taskId task ID
     * @param type container type (strategy, plugin, listener)
     * @param offset starting line number (0-based)
     * @param limit maximum number of lines to return
     * @return list of log lines
     */
    List<String> getTaskLogs(Long taskId, String type, int offset, int limit);

    /**
     * Get the last N lines of task logs for a specific container type.
     *
     * @param taskId task ID
     * @param type container type (strategy, plugin, listener)
     * @param lines number of lines to return from the end
     * @return list of log lines
     */
    List<String> getLogTail(Long taskId, String type, int lines);

    /**
     * Search logs for keyword in a specific container type.
     * Returns all lines containing the keyword.
     *
     * @param taskId task ID
     * @param type container type (strategy, plugin, listener)
     * @param keyword keyword to search for
     * @param limit maximum number of results (optional, 0 for no limit)
     * @return list of matching log lines
     */
    List<String> searchLogs(Long taskId, String type, String keyword, int limit);

    /**
     * Get all logs for a task across all container types.
     *
     * @param taskId task ID
     * @return map with keys "strategy", "plugin", "listener" and values as lists of log lines
     */
    Map<String, List<String>> getAllTaskLogs(Long taskId);

    /**
     * Get log file metadata for a specific container type.
     *
     * @param taskId task ID
     * @param type container type (strategy, plugin, listener)
     * @return log metadata
     */
    LogMetadata getLogMetadata(Long taskId, String type);

    /**
     * Get log file metadata for all container types.
     *
     * @param taskId task ID
     * @return map with keys "strategy", "plugin", "listener" and values as LogMetadata
     */
    Map<String, LogMetadata> getAllLogMetadata(Long taskId);

    /**
     * Ensure log directory exists for a task.
     * Creates directory structure: /var/log/taskm/tasks/task_{id}/
     *
     * @param taskId task ID
     */
    void ensureLogDirectory(Long taskId);

    /**
     * Log file metadata.
     */
    class LogMetadata {
        private final String filePath;
        private final long fileSize;
        private final long lineCount;
        private final String lastModified;

        public LogMetadata(String filePath, long fileSize, long lineCount, String lastModified) {
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.lineCount = lineCount;
            this.lastModified = lastModified;
        }

        public String getFilePath() {
            return filePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public long getLineCount() {
            return lineCount;
        }

        public String getLastModified() {
            return lastModified;
        }
    }
}
