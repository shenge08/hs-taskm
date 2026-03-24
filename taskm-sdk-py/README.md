# TaskM Python SDK

Python SDK for calling data plugin and listener HTTP APIs in the TaskM system.

## Installation

```bash
pip install taskm-sdk-py
```

Or install from source:

```bash
git clone https://github.com/taskm/taskm-sdk-py.git
cd taskm-sdk-py
pip install -e .
```

## Overview

TaskM SDK provides Python utilities for:

- **DataPluginClient**: Call data plugin instances to fetch market data
- **ListenerClient**: Notify listeners about task lifecycle events
- **Environment**: Convenient environment variable access with type conversion

All clients automatically read configuration from environment variables and handle errors with detailed exceptions.

## Quick Start

### Using DataPluginClient

```python
import os
from taskm_sdk import DataPluginClient

# Initialize client (reads PLUGIN_ENDPOINT from environment)
plugin = DataPluginClient()

# Or specify endpoint explicitly
# plugin = DataPluginClient(endpoint="http://plugin-1:8080/instances/prod/api")

# Fetch data from plugin
data = plugin.get_data(
    symbol="BTC/USDT",
    interval="1h"
)

print(f"Received data: {data}")
```

### Using ListenerClient

```python
import os
from taskm_sdk import ListenerClient

# Initialize client (reads LISTENER_ENDPOINT and TASK_ID from environment)
listener = ListenerClient()

# Or specify parameters explicitly
# listener = ListenerClient(
#     endpoint="http://listener-1:8080/instances/prod/api",
#     task_id=123
# )

# Notify task lifecycle events
listener.on_task_started()

try:
    # Execute strategy logic
    result = execute_strategy()

    # Notify completion
    listener.on_task_completed(result={"profit": 100.5})

except Exception as e:
    # Notify failure
    listener.on_task_failed(error=str(e))
```

### Complete Example

```python
import os
from taskm_sdk import DataPluginClient, ListenerClient
from taskm_sdk.exceptions import (
    PluginTimeoutException,
    PluginApiException,
    ListenerConnectionException,
)

def execute_strategy():
    """Execute trading strategy using TaskM SDK."""

    # Initialize clients
    plugin = DataPluginClient(timeout=30)
    listener = ListenerClient()

    try:
        # Notify task started
        listener.on_task_started()
        print("Task started")

        # Fetch market data
        data = plugin.get_data(symbol="BTC/USDT", interval="1h")
        print(f"Received data: {data}")

        # Execute trading logic
        result = {
            "profit": 100.5,
            "trades": 5,
            "timestamp": data.get("timestamp")
        }

        # Notify task completed
        listener.on_task_completed(result)
        print(f"Task completed: {result}")

    except PluginTimeoutException as e:
        print(f"Plugin request timed out: {e}")
        listener.on_task_failed(error=f"Plugin timeout: {e}")

    except PluginApiException as e:
        print(f"Plugin API error: {e}")
        listener.on_task_failed(error=f"Plugin error: {e}")

    except ListenerConnectionException as e:
        print(f"Failed to notify listener: {e}")

    except Exception as e:
        print(f"Unexpected error: {e}")
        listener.on_task_failed(error=str(e))

if __name__ == "__main__":
    execute_strategy()
```

### Using Environment Utility

```python
from taskm_sdk import Environment

# Get environment variables with type conversion
task_id = Environment.get_int("TASK_ID", default=0)
log_type = Environment.get_string("LOG_TYPE", default="strategy")
enabled = Environment.get_bool("ENABLED", default=False)
timeout = Environment.get_int("TIMEOUT", default=30)

# Get required variables (raises exception if not set)
api_key = Environment.get_required("API_KEY")

# Get all variables with prefix
taskm_vars = Environment.get_all(prefix="TASKM_")
print(f"TaskM config: {taskm_vars}")

# Check if variable exists
if Environment.exists("DEBUG"):
    debug_mode = Environment.get_bool("DEBUG")
```

## Configuration

### Environment Variables

The SDK reads configuration from the following environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `PLUGIN_ENDPOINT` | Data plugin API endpoint | `http://plugin-1:8080/instances/prod/api` |
| `LISTENER_ENDPOINT` | Listener API endpoint | `http://listener-1:8080/instances/prod/api` |
| `TASK_ID` | Current task ID | `123` |

### Client Options

Both clients support the following initialization parameters:

```python
# DataPluginClient
plugin = DataPluginClient(
    endpoint="http://plugin-1:8080/instances/prod/api",  # Optional, reads from env
    timeout=30  # Request timeout in seconds (default: 30)
)

# ListenerClient
listener = ListenerClient(
    endpoint="http://listener-1:8080/instances/prod/api",  # Optional, reads from env
    task_id=123,  # Optional, reads from env
    timeout=30  # Request timeout in seconds (default: 30)
)
```

## API Reference

### Environment

The `Environment` class provides convenient methods for accessing environment variables with automatic type conversion.

#### `get(name: str, default=None, type=None) -> Optional[T]`

Get an environment variable with optional type conversion and default value.

**Parameters:**
- `name`: Environment variable name
- `default`: Default value if not set
- `type`: Type to convert to (str, int, float, bool)

**Returns:**
- The converted value or default

**Example:**
```python
# Get with type conversion
task_id = Environment.get("TASK_ID", type=int)  # Returns int or None
port = Environment.get("PORT", default=8080, type=int)  # Returns int or 8080
enabled = Environment.get("ENABLED", type=bool)  # Returns bool or None
```

#### Type-Specific Methods

- `get_string(name, default=None)`: Get as string
- `get_int(name, default=None)`: Get as integer
- `get_float(name, default=None)`: Get as float
- `get_bool(name, default=None)`: Get as boolean

**Boolean Conversion:**
Accepts: `true/false`, `1/0`, `yes/no`, `on/off` (case-insensitive)

**Example:**
```python
task_id = Environment.get_int("TASK_ID", default=0)
price = Environment.get_float("PRICE", default=0.0)
enabled = Environment.get_bool("ENABLED", default=False)
```

#### `get_required(name: str, type=None) -> T`

Get a required environment variable (raises exception if not set).

**Example:**
```python
api_key = Environment.get_required("API_KEY")
port = Environment.get_required("PORT", type=int)
```

#### `get_all(prefix=None) -> Dict[str, str]`

Get all environment variables, optionally filtered by prefix.

**Example:**
```python
all_vars = Environment.get_all()
taskm_vars = Environment.get_all(prefix="TASKM_")
```

#### `exists(name: str) -> bool`

Check if an environment variable is set.

**Example:**
```python
if Environment.exists("DEBUG"):
    debug_mode = Environment.get_bool("DEBUG")
```

### DataPluginClient

#### `get_data(**params) -> Dict[str, Any]`

Fetch data from the plugin.

**Parameters:**
- `**params`: Arbitrary keyword arguments (e.g., `symbol`, `interval`, `apiKey`)

**Returns:**
- Dictionary with plugin response data

**Raises:**
- `PluginConnectionException`: Connection failed
- `PluginTimeoutException`: Request timed out
- `PluginApiException`: API returned error
- `PluginResponseException`: Invalid response format

#### `health_check() -> Dict[str, Any]`

Check plugin health status.

**Returns:**
- Dictionary with health status

### ListenerClient

#### `on_task_started() -> Dict[str, Any]`

Notify that the task has started.

**Returns:**
- Dictionary with listener response

#### `on_task_completed(result: Dict[str, Any]) -> Dict[str, Any]`

Notify that the task completed successfully.

**Parameters:**
- `result`: Task execution result (e.g., `{"profit": 100.5}`)

**Returns:**
- Dictionary with listener response

#### `on_task_failed(error: str) -> Dict[str, Any]`

Notify that the task has failed.

**Parameters:**
- `error`: Error message

**Returns:**
- Dictionary with listener response

## Exception Handling

The SDK provides detailed exceptions for error handling:

### Plugin Exceptions

```python
from taskm_sdk.exceptions import (
    PluginConnectionException,
    PluginTimeoutException,
    PluginApiException,
    PluginResponseException,
)

try:
    data = plugin.get_data(symbol="BTC/USDT")
except PluginTimeoutException as e:
    print(f"Timeout: {e.timeout}s, endpoint: {e.endpoint}")
except PluginApiException as e:
    print(f"API Error: status={e.status_code}, response={e.response_body}")
except PluginConnectionException as e:
    print(f"Connection Error: {e.message}, endpoint: {e.endpoint}")
```

### Listener Exceptions

```python
from taskm_sdk.exceptions import (
    ListenerConnectionException,
    ListenerTimeoutException,
    ListenerApiException,
)

try:
    listener.on_task_completed(result)
except ListenerTimeoutException as e:
    print(f"Timeout: {e.timeout}s")
except ListenerApiException as e:
    print(f"API Error: status={e.status_code}")
except ListenerConnectionException as e:
    print(f"Connection Error: {e.message}")
```

## Development

### Running Tests

```bash
# Install development dependencies
pip install -e .[dev]

# Run tests
pytest

# Run tests with coverage
pytest --cov=taskm_sdk --cov-report=html
```

### Code Formatting

```bash
# Format code with black
black taskm_sdk tests

# Lint with flake8
flake8 taskm_sdk tests

# Type checking with mypy
mypy taskm_sdk
```

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Support

- Documentation: https://github.com/taskm/taskm-sdk-py
- Bug Reports: https://github.com/taskm/taskm-sdk-py/issues
- Source Code: https://github.com/taskm/taskm-sdk-py
