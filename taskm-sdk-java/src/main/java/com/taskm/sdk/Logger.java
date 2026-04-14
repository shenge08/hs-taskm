package com.taskm.sdk;

import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Logger facade for TaskM SDK.
 *
 * <p>This class provides a simple logging API that wraps SLF4J, the standard logging facade for Java.
 * It supports structured logging with key-value pairs and reads log level from environment variables.</p>
 *
 * <h3>Environment variables:</h3>
 * <ul>
 *   <li>LOG_LEVEL: Logging level (TRACE, DEBUG, INFO, WARN, ERROR, default: INFO)</li>
 *   <li>LOG_FORMAT: Log format (text, json, default: text) - Note: JSON format requires Logback config</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * import com.taskm.sdk.Logger;
 *
 * Logger logger = new Logger("MyStrategy");
 * logger.info("Strategy started");
 * logger.debug("Processing data", "symbol", "BTC/USDT", "price", 50000);
 * logger.error("Failed to execute", "error", exception.getMessage());
 * }</pre>
 *
 * <h3>Logback Configuration (in application):</h3>
 * <p>Your application using this SDK should provide a Logback configuration file
 * (logback.xml or logback-spring.xml) to control log output format and destination.</p>
 *
 * @since 1.0.0
 */
public final class Logger {

    private final org.slf4j.Logger slf4jLogger;

    /**
     * Create a new logger with the given name.
     *
     * @param name the logger name (usually strategy or component name)
     */
    public Logger(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Logger name cannot be null or empty");
        }
        this.slf4jLogger = LoggerFactory.getLogger(name);
    }

    /**
     * Log a debug message.
     *
     * @param message the log message
     */
    public void debug(String message) {
        if (slf4jLogger.isDebugEnabled()) {
            slf4jLogger.debug(message);
        }
    }

    /**
     * Log a debug message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void debug(String message, String key, Object value) {
        if (slf4jLogger.isDebugEnabled()) {
            slf4jLogger.debug(formatMessage(message, key, value));
        }
    }

    /**
     * Log a debug message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void debug(String message, Map<String, Object> data) {
        if (slf4jLogger.isDebugEnabled()) {
            slf4jLogger.debug(formatMessage(message, data));
        }
    }

    /**
     * Log an info message.
     *
     * @param message the log message
     */
    public void info(String message) {
        if (slf4jLogger.isInfoEnabled()) {
            slf4jLogger.info(message);
        }
    }

    /**
     * Log an info message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void info(String message, String key, Object value) {
        if (slf4jLogger.isInfoEnabled()) {
            slf4jLogger.info(formatMessage(message, key, value));
        }
    }

    /**
     * Log an info message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void info(String message, Map<String, Object> data) {
        if (slf4jLogger.isInfoEnabled()) {
            slf4jLogger.info(formatMessage(message, data));
        }
    }

    /**
     * Log a warning message.
     *
     * @param message the log message
     */
    public void warning(String message) {
        if (slf4jLogger.isWarnEnabled()) {
            slf4jLogger.warn(message);
        }
    }

    /**
     * Log a warning message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void warning(String message, String key, Object value) {
        if (slf4jLogger.isWarnEnabled()) {
            slf4jLogger.warn(formatMessage(message, key, value));
        }
    }

    /**
     * Log a warning message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void warning(String message, Map<String, Object> data) {
        if (slf4jLogger.isWarnEnabled()) {
            slf4jLogger.warn(formatMessage(message, data));
        }
    }

    /**
     * Log an error message.
     *
     * @param message the log message
     */
    public void error(String message) {
        slf4jLogger.error(message);
    }

    /**
     * Log an error message with additional data.
     *
     * @param message the log message
     * @param key a data key
     * @param value a data value
     */
    public void error(String message, String key, Object value) {
        slf4jLogger.error(formatMessage(message, key, value));
    }

    /**
     * Log an error message with additional data.
     *
     * @param message the log message
     * @param data additional data as key-value pairs
     */
    public void error(String message, Map<String, Object> data) {
        slf4jLogger.error(formatMessage(message, data));
    }

    /**
     * Log an error message with exception.
     *
     * @param message the log message
     * @param throwable the exception
     */
    public void error(String message, Throwable throwable) {
        slf4jLogger.error(message, throwable);
    }

    /**
     * Log an error message with exception and additional data.
     *
     * @param message the log message
     * @param throwable the exception
     * @param data additional data as key-value pairs
     */
    public void error(String message, Throwable throwable, Map<String, Object> data) {
        slf4jLogger.error(formatMessage(message, data), throwable);
    }

    /**
     * Check if debug logging is enabled.
     *
     * @return true if debug logging is enabled
     */
    public boolean isDebugEnabled() {
        return slf4jLogger.isDebugEnabled();
    }

    /**
     * Check if info logging is enabled.
     *
     * @return true if info logging is enabled
     */
    public boolean isInfoEnabled() {
        return slf4jLogger.isInfoEnabled();
    }

    /**
     * Format message with key-value pair.
     */
    private String formatMessage(String message, String key, Object value) {
        return message + " [" + key + "=" + value + "]";
    }

    /**
     * Format message with data map.
     */
    private String formatMessage(String message, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return message;
        }

        StringBuilder sb = new StringBuilder(message);
        sb.append(" ");

        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;

            sb.append(entry.getKey())
              .append("=")
              .append(entry.getValue());
        }

        return sb.toString();
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
