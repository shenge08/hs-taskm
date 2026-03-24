# TaskM Java SDK

Java SDK for calling data plugin and listener HTTP APIs in the TaskM system.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.taskm</groupId>
    <artifactId>sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or for Gradle:

```groovy
implementation 'com.taskm:sdk:1.0.0'
```

## Overview

TaskM SDK provides Java utilities for:

- **DataPluginClient**: Call data plugin instances to fetch market data
- **ListenerClient**: Notify listeners about task lifecycle events
- **Environment**: Convenient environment variable access with type conversion

All clients automatically read configuration from environment variables and handle errors with detailed exceptions.

## Quick Start

### Using DataPluginClient

```java
import com.taskm.sdk.DataPluginClient;
import java.util.Map;

// Initialize client (reads PLUGIN_ENDPOINT from environment)
DataPluginClient plugin = new DataPluginClient();

// Or specify endpoint explicitly
// DataPluginClient plugin = new DataPluginClient("http://plugin-1:8080/instances/prod/api");

// Fetch data from plugin
Map<String, Object> data = plugin.getData(
    Map.of("symbol", "BTC/USDT", "interval", "1h")
);

System.out.println("Received data: " + data);
```

### Using ListenerClient

```java
import com.taskm.sdk.ListenerClient;
import java.util.Map;

// Initialize client (reads LISTENER_ENDPOINT and TASK_ID from environment)
ListenerClient listener = new ListenerClient();

// Or specify parameters explicitly
// ListenerClient listener = new ListenerClient(
//     "http://listener-1:8080/instances/prod/api",
//     123L
// );

// Notify task lifecycle events
listener.onTaskStarted();

try {
    // Execute strategy logic
    Map<String, Object> result = executeStrategy();

    // Notify completion
    listener.onTaskCompleted(result);

} catch (Exception e) {
    // Notify failure
    listener.onTaskFailed(e.getMessage());
}
```

### Complete Example

```java
import com.taskm.sdk.DataPluginClient;
import com.taskm.sdk.ListenerClient;
import com.taskm.sdk.exception.*;

import java.util.Map;

public class StrategyExecutor {

    public void execute() {
        DataPluginClient plugin = new DataPluginClient();
        ListenerClient listener = new ListenerClient();

        try {
            // Notify task started
            listener.onTaskStarted();
            System.out.println("Task started");

            // Fetch market data
            Map<String, Object> data = plugin.getData(
                Map.of("symbol", "BTC/USDT", "interval", "1h")
            );
            System.out.println("Received data: " + data);

            // Execute trading logic
            Map<String, Object> result = Map.of(
                "profit", 100.5,
                "trades", 5,
                "timestamp", data.get("timestamp")
            );

            // Notify task completed
            listener.onTaskCompleted(result);
            System.out.println("Task completed: " + result);

        } catch (PluginTimeoutException e) {
            System.err.println("Plugin request timed out: " + e.getMessage());
            listener.onTaskFailed("Plugin timeout: " + e.getMessage());

        } catch (PluginApiException e) {
            System.err.println("Plugin API error: " + e.getMessage());
            listener.onTaskFailed("Plugin error: " + e.getMessage());

        } catch (ListenerConnectionException e) {
            System.err.println("Failed to notify listener: " + e.getMessage());

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            listener.onTaskFailed(e.getMessage());
        }
    }
}
```

### Using Environment Utility

```java
import com.taskm.sdk.Environment;
import java.util.Map;

// Get environment variables with type conversion
int taskId = Environment.getInt("TASK_ID", 0);
String logType = Environment.get("LOG_TYPE", "strategy");
boolean enabled = Environment.getBoolean("ENABLED", false);
int timeout = Environment.getInt("TIMEOUT", 30);

// Get required variables (throws exception if not set)
String apiKey = Environment.getRequired("API_KEY");

// Get all variables with prefix
Map<String, String> taskmVars = Environment.getAll("TASKM_");
System.out.println("TaskM config: " + taskmVars);

// Check if variable exists
if (Environment.exists("DEBUG")) {
    boolean debugMode = Environment.getBoolean("DEBUG", false);
}
```

## Configuration

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `PLUGIN_ENDPOINT` | Data plugin API endpoint | `http://plugin-1:8080/instances/prod/api` |
| `LISTENER_ENDPOINT` | Listener API endpoint | `http://listener-1:8080/instances/prod/api` |
| `TASK_ID` | Current task ID | `123` |

### Client Options

```java
// DataPluginClient
DataPluginClient plugin = new DataPluginClient(
    "http://plugin-1:8080/instances/prod/api",  // endpoint (required)
    30  // timeout in seconds (optional, default: 30)
);

// ListenerClient
ListenerClient listener = new ListenerClient(
    "http://listener-1:8080/instances/prod/api",  // endpoint (required)
    123L,  // task ID (required)
    30  // timeout in seconds (optional, default: 30)
);
```

## API Reference

### Environment

The `Environment` class provides static utility methods for accessing environment variables with automatic type conversion.

#### Type-Specific Get Methods

```java
// String
String value = Environment.get("NAME", "default");

// Integer
int port = Environment.getInt("PORT", 8080);
int taskId = Environment.getRequiredInt("TASK_ID");

// Long
long size = Environment.getLong("SIZE", 1024L);

// Float/Double
double price = Environment.getDouble("PRICE", 0.0);

// Boolean (accepts: true/false, 1/0, yes/no, on/off)
boolean enabled = Environment.getBoolean("ENABLED", false);
```

#### Required Variables

```java
String apiKey = Environment.getRequired("API_KEY");
int port = Environment.getRequiredInt("PORT");
```

#### Query Methods

```java
Map<String, String> allVars = Environment.getAll();
Map<String, String> taskmVars = Environment.getAll("TASKM_");
boolean hasDebug = Environment.exists("DEBUG");
```

#### List Methods

```java
List<String> items = Environment.getCommaSeparatedList("ITEMS");
List<String> hosts = Environment.getList("HOSTS", ":");
```

### DataPluginClient

#### Constructor

```java
DataPluginClient(String endpoint)
DataPluginClient(String endpoint, int timeoutSeconds)
```

#### Methods

**`getData(Map<String, Object> params) -> Map<String, Object>`**

Fetch data from the plugin.

*Parameters:*
- `params`: Arbitrary parameters (e.g., `symbol`, `interval`, `apiKey`)

*Returns:* Map with plugin response data

*Throws:*
- `PluginConnectionException`: Connection failed
- `PluginTimeoutException`: Request timed out
- `PluginApiException`: API returned error

**`healthCheck() -> Map<String, Object>`**

Check plugin health status.

*Returns:* Map with health status

*Throws:*
- `PluginConnectionException`: Connection failed
- `PluginTimeoutException`: Request timed out

### ListenerClient

#### Constructor

```java
ListenerClient(String endpoint, Long taskId)
ListenerClient(String endpoint, Long taskId, int timeoutSeconds)
```

#### Methods

**`onTaskStarted() -> Map<String, Object>`**

Notify that the task has started.

*Returns:* Map with listener response

*Throws:*
- `ListenerConnectionException`: Connection failed
- `ListenerTimeoutException`: Request timed out
- `ListenerApiException`: API returned error

**`onTaskCompleted(Map<String, Object> result) -> Map<String, Object>`**

Notify that the task completed successfully.

*Parameters:*
- `result`: Task execution result

*Returns:* Map with listener response

*Throws:* Same as `onTaskStarted()`

**`onTaskFailed(String error) -> Map<String, Object>`**

Notify that the task has failed.

*Parameters:*
- `error`: Error message

*Returns:* Map with listener response

*Throws:* Same as `onTaskStarted()`

## Exception Handling

### Plugin Exceptions

```java
try {
    Map<String, Object> data = plugin.getData(params);
} catch (PluginTimeoutException e) {
    System.err.println("Timeout: " + e.getTimeout() + "s, endpoint: " + e.getEndpoint());
} catch (PluginApiException e) {
    System.err.println("API Error: status=" + e.getStatusCode() + ", response=" + e.getResponseBody());
} catch (PluginConnectionException e) {
    System.err.println("Connection Error: " + e.getMessage() + ", endpoint: " + e.getEndpoint());
}
```

### Listener Exceptions

```java
try {
    listener.onTaskCompleted(result);
} catch (ListenerTimeoutException e) {
    System.err.println("Timeout: " + e.getTimeout() + "s");
} catch (ListenerApiException e) {
    System.err.println("API Error: status=" + e.getStatusCode());
} catch (ListenerConnectionException e) {
    System.err.println("Connection Error: " + e.getMessage());
}
```

## Requirements

- Java 11 or higher
- Maven 3.6 or higher (for building from source)

## Building from Source

```bash
git clone https://github.com/taskm/taskm-sdk-java.git
cd taskm-sdk-java
mvn clean install
```

## Running Tests

```bash
mvn test
```

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Support

- Documentation: https://github.com/taskm/taskm-sdk-java
- Bug Reports: https://github.com/taskm/taskm-sdk-java/issues
- Source Code: https://github.com/taskm/taskm-sdk-java
