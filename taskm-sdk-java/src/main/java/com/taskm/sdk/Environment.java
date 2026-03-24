package com.taskm.sdk;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for accessing environment variables with type conversion.
 *
 * <p>This class provides convenient methods to read environment variables
 * with automatic type conversion and default values.
 *
 * <h3>Common environment variables in HS-TASKM:</h3>
 * <ul>
 *   <li>TASK_ID: Task execution ID</li>
 *   <li>LOG_TYPE: Log type (strategy, plugin, listener)</li>
 *   <li>PLUGIN_ENDPOINT: Plugin instance HTTP endpoint</li>
 *   <li>LISTENER_ENDPOINT: Listener instance HTTP endpoint</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * import com.taskm.sdk.Environment;
 *
 * // Get with type conversion and default value
 * int taskId = Environment.getInt("TASK_ID", 0);
 * String logType = Environment.get("LOG_TYPE", "strategy");
 * boolean enabled = Environment.getBoolean("ENABLED", false);
 *
 * // Get required variable (throws exception if not set)
 * String apiKey = Environment.getRequired("API_KEY");
 *
 * // Get all variables with prefix
 * Map<String, String> taskmVars = Environment.getAll("TASKM_");
 *
 * // Check if variable exists
 * if (Environment.exists("DEBUG")) {
 *     // ...
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public final class Environment {

    // Private constructor to prevent instantiation
    private Environment() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Get an environment variable as a string.
     *
     * @param name the environment variable name
     * @param defaultValue the default value if variable is not set
     * @return the environment variable value, or the default value if not set
     */
    public static String get(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Get an environment variable as a string.
     *
     * @param name the environment variable name
     * @return the environment variable value, or null if not set
     */
    public static String get(String name) {
        return System.getenv(name);
    }

    /**
     * Get an environment variable as an integer.
     *
     * @param name the environment variable name
     * @param defaultValue the default value if variable is not set
     * @return the environment variable value as an integer, or the default value
     * @throws IllegalArgumentException if the value cannot be converted to an integer
     */
    public static int getInt(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Environment variable '%s' value '%s' cannot be converted to int",
                    name, value), e);
        }
    }

    /**
     * Get an environment variable as a long.
     *
     * @param name the environment variable name
     * @param defaultValue the default value if variable is not set
     * @return the environment variable value as a long, or the default value
     * @throws IllegalArgumentException if the value cannot be converted to a long
     */
    public static long getLong(String name, long defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Environment variable '%s' value '%s' cannot be converted to long",
                    name, value), e);
        }
    }

    /**
     * Get an environment variable as a float.
     *
     * @param name the environment variable name
     * @param defaultValue the default value if variable is not set
     * @return the environment variable value as a float, or the default value
     * @throws IllegalArgumentException if the value cannot be converted to a float
     */
    public static float getFloat(String name, float defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Environment variable '%s' value '%s' cannot be converted to float",
                    name, value), e);
        }
    }

    /**
     * Get an environment variable as a double.
     *
     * @param name the environment variable name
     * @param defaultValue the default value if variable is not set
     * @return the environment variable value as a double, or the default value
     * @throws IllegalArgumentException if the value cannot be converted to a double
     */
    public static double getDouble(String name, double defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Environment variable '%s' value '%s' cannot be converted to double",
                    name, value), e);
        }
    }

    /**
     * Get an environment variable as a boolean.
     *
     * <p>Accepts: true/false, 1/0, yes/no, on/off (case-insensitive)
     *
     * @param name the environment variable name
     * @param defaultValue the default value if variable is not set
     * @return the environment variable value as a boolean, or the default value
     * @throws IllegalArgumentException if the value cannot be converted to a boolean
     */
    public static boolean getBoolean(String name, boolean defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        }

        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "true":
            case "1":
            case "yes":
            case "on":
            case "t":
            case "y":
                return true;
            case "false":
            case "0":
            case "no":
            case "off":
            case "f":
            case "n":
            case "":
                return false;
            default:
                throw new IllegalArgumentException(
                    String.format("Environment variable '%s' value '%s' cannot be converted to boolean",
                        name, value));
        }
    }

    /**
     * Get a required environment variable.
     *
     * @param name the environment variable name
     * @return the environment variable value
     * @throws IllegalArgumentException if the environment variable is not set
     */
    public static String getRequired(String name) {
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("Required environment variable '%s' is not set", name));
        }
        return value;
    }

    /**
     * Get a required environment variable as an integer.
     *
     * @param name the environment variable name
     * @return the environment variable value as an integer
     * @throws IllegalArgumentException if the environment variable is not set
     *          or cannot be converted to an integer
     */
    public static int getRequiredInt(String name) {
        String value = getRequired(name);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Environment variable '%s' value '%s' cannot be converted to int",
                    name, value), e);
        }
    }

    /**
     * Get a required environment variable as a long.
     *
     * @param name the environment variable name
     * @return the environment variable value as a long
     * @throws IllegalArgumentException if the environment variable is not set
     *          or cannot be converted to a long
     */
    public static long getRequiredLong(String name) {
        String value = getRequired(name);
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Environment variable '%s' value '%s' cannot be converted to long",
                    name, value), e);
        }
    }

    /**
     * Get a required environment variable as a double.
     *
     * @param name the environment variable name
     * @return the environment variable value as a double
     * @throws IllegalArgumentException if the environment variable is not set
     *          or cannot be converted to a double
     */
    public static double getRequiredDouble(String name) {
        String value = getRequired(name);
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Environment variable '%s' value '%s' cannot be converted to double",
                    name, value), e);
        }
    }

    /**
     * Get a required environment variable as a boolean.
     *
     * @param name the environment variable name
     * @return the environment variable value as a boolean
     * @throws IllegalArgumentException if the environment variable is not set
     *          or cannot be converted to a boolean
     */
    public static boolean getRequiredBoolean(String name) {
        String value = getRequired(name);
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "true":
            case "1":
            case "yes":
            case "on":
            case "t":
            case "y":
                return true;
            case "false":
            case "0":
            case "no":
            case "off":
            case "f":
            case "n":
            case "":
                return false;
            default:
                throw new IllegalArgumentException(
                    String.format("Environment variable '%s' value '%s' cannot be converted to boolean",
                        name, value));
        }
    }

    /**
     * Get all environment variables.
     *
     * @return a map of all environment variable names and values
     */
    public static Map<String, String> getAll() {
        return new HashMap<>(System.getenv());
    }

    /**
     * Get all environment variables with a specific prefix.
     *
     * @param prefix the prefix to filter variables (e.g., "TASKM_")
     * @return a map of environment variable names and values with the specified prefix
     */
    public static Map<String, String> getAll(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getAll();
        }

        return System.getenv().entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(prefix))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                HashMap::new
            ));
    }

    /**
     * Check if an environment variable is set.
     *
     * @param name the environment variable name
     * @return true if the variable is set, false otherwise
     */
    public static boolean exists(String name) {
        return System.getenv(name) != null;
    }

    /**
     * Check if any of the specified environment variables are set.
     *
     * @param names the environment variable names to check
     * @return true if any of the variables are set, false otherwise
     */
    public static boolean existsAny(String... names) {
        if (names == null || names.length == 0) {
            return false;
        }
        for (String name : names) {
            if (exists(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if all of the specified environment variables are set.
     *
     * @param names the environment variable names to check
     * @return true if all of the variables are set, false otherwise
     */
    public static boolean existsAll(String... names) {
        if (names == null || names.length == 0) {
            return true;
        }
        for (String name : names) {
            if (!exists(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get environment variable value split by delimiter.
     *
     * <p>Useful for comma-separated or colon-separated values.
     *
     * @param name the environment variable name
     * @param delimiter the delimiter to split on (e.g., ",", ":")
     * @return a list of split values, or an empty list if the variable is not set
     */
    public static List<String> getList(String name, String delimiter) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(value.split(delimiter))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * Get environment variable value split by comma.
     *
     * @param name the environment variable name
     * @return a list of comma-separated values, or an empty list if the variable is not set
     */
    public static List<String> getCommaSeparatedList(String name) {
        return getList(name, ",");
    }
}
