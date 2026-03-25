package com.taskm.sdk;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple logger for TaskM strategy execution.
 *
 * <p>This logger provides convenient logging methods with automatic configuration
 * from environment variables. It supports both plain text and JSON logging.
 *
 * <h3>Environment variables:</h3>
 * <ul>
 *   <li>LOG_LEVEL: Logging level (DEBUG, INFO, WARNING, ERROR, default: INFO)</li>
 *   <li>LOG_FORMAT: Log format (text, json, default: text)</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * import com.taskm.sdk.Logger;
 *
 * Logger logger = new Logger("MyStrategy");
 * logger.info("Strategy started");
 * logger.debug("Processing data", "symbol", "BTC/USDT");
 * logger.error("Failed to execute", "error", exception.getMessage());
 * }</pre>
 *
 * @since 1.0.0
 */
public final class Logger {

    /**
     * Log level enumeration.
     */
    public enum Level {
        DEBUG(0),
        INFO(1),
        WARNING(2),
        ERROR(3),
        CRITICAL(4);

        private final int value;

        Level(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Log format enumeration.
     */
    public enum Format {
        TEXT,
        JSON
    }

    private final String name;
    private final Level level;
    private final Format format;
    private final DateTimeFormatter timeFormatter;

    /**
     * Create a new logger with the given name.
     *
     * @param name the logger name (usually strategy or component name)
     */
    public Logger(String name) {
        this(name, null, null);
    }

    /**
     * Create a new logger with the given name and level.
     *
     * @param name the logger name
     * @param level the log level (if null, reads from LOG_LEVEL env var)
     */
    public Logger(String name, Level level) {
        this(name, level, null);
    }

    /**
     * Create a new logger with the given name, level, and format.
     *
     * @param name the logger name
     * @param level the log level (if null, reads from LOG_LEVEL env var)
     * @param format the log format (if null, reads from LOG_FORMAT env var)
     */
    public Logger(String name, Level level, Format format) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Logger name cannot be null or empty");
        }

        this.name = name;

        // Determine level from parameter or environment
        if (level == null) {
            String levelStr = Environment.get("LOG_LEVEL", "INFO").toUpperCase();
            this.level = parseLevel(levelStr);
        } else {
            this.level = level;
        }

        // Determine format from parameter or environment
        if (format == null) {
            String formatStr = Environment.get("LOG_FORMAT", "text").toLowerCase();
            this.format = "json".equals(formatStr) ? Format.JSON : Format.TEXT;
        } else {
            this.format = format;
        }

        this.timeFormatter = DateTimeFormatter.ISO_INSTANT;
    }

    /**
     * Log a debug message.
     *
     * @param message the log message
     */
    public void debug(String message) {
        log(Level.DEBUG, message, null);
    }

    /**
     * Log a debug message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void debug(String message, String key, Object value) {
        log(Level.DEBUG, message, Map.of(key, value));
    }

    /**
     * Log a debug message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void debug(String message, Map<String, Object> data) {
        log(Level.DEBUG, message, data);
    }

    /**
     * Log an info message.
     *
     * @param message the log message
     */
    public void info(String message) {
        log(Level.INFO, message, null);
    }

    /**
     * Log an info message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void info(String message, String key, Object value) {
        log(Level.INFO, message, Map.of(key, value));
    }

    /**
     * Log an info message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void info(String message, Map<String, Object> data) {
        log(Level.INFO, message, data);
    }

    /**
     * Log a warning message.
     *
     * @param message the log message
     */
    public void warning(String message) {
        log(Level.WARNING, message, null);
    }

    /**
     * Log a warning message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void warning(String message, String key, Object value) {
        log(Level.WARNING, message, Map.of(key, value));
    }

    /**
     * Log a warning message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void warning(String message, Map<String, Object> data) {
        log(Level.WARNING, message, data);
    }

    /**
     * Log an error message.
     *
     * @param message the log message
     */
    public void error(String message) {
        log(Level.ERROR, message, null);
    }

    /**
     * Log an error message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void error(String message, String key, Object value) {
        log(Level.ERROR, message, Map.of(key, value));
    }

    /**
     * Log an error message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void error(String message, Map<String, Object> data) {
        log(Level.ERROR, message, data);
    }

    /**
     * Log a critical message.
     *
     * @param message the log message
     */
    public void critical(String message) {
        log(Level.CRITICAL, message, null);
    }

    /**
     * Log a critical message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void critical(String message, String key, Object value) {
        log(Level.CRITICAL, message, Map.of(key, value));
    }

    /**
     * Log a critical message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void critical(String message, Map<String, Object> data) {
        log(Level.CRITICAL, message, data);
    }

    /**
     * Internal logging method.
     */
    private void log(Level level, String message, Map<String, Object> data) {
        if (level.getValue() < this.level.getValue()) {
            return; // Skip logs below the configured level
        }

        String timestamp = timeFormatter.format(Instant.now());

        if (format == Format.JSON) {
            // JSON format
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", timestamp);
            logEntry.put("logger", name);
            logEntry.put("level", level.name());
            logEntry.put("message", message);
            if (data != null && !data.isEmpty()) {
                logEntry.put("data", data);
            }
            System.out.println(toJson(logEntry));

        } else {
            // Text format
            StringBuilder sb = new StringBuilder();
            sb.append(timestamp).append(" - ");
            sb.append(name).append(" - ");
            sb.append(level.name()).append(" - ");
            sb.append(message);

            if (data != null && !data.isEmpty()) {
                sb.append(" ").append(data);
            }

            System.out.println(sb.toString());
        }
    }

    /**
     * Convert map to simple JSON string.
     */
    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;

            sb.append("\"").append(entry.getKey()).append("\": ");

            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else if (value instanceof Map) {
                sb.append(toJson((Map<String, Object>) value));
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Escape special characters in JSON string.
     */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Parse level string to Level enum.
     */
    private Level parseLevel(String levelStr) {
        try {
            return Level.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Level.INFO; // Default to INFO if invalid
        }
    }

    /**
     * Get a logger instance with the given name.
     *
     * <p>This is a convenience method for quickly creating a logger.
     *
     * @param name the logger name
     * @return a new Logger instance
     */
    public static Logger getLogger(String name) {
        return new Logger(name);
    }
}
