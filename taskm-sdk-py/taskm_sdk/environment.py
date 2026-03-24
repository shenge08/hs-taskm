"""
Environment variable utilities for HS-TASKM SDK.

This module provides convenient methods for accessing environment variables
with type conversion and default value support.
"""

import os
from typing import Any, Dict, Optional, TypeVar, Type, Union

T = TypeVar('T', str, int, float, bool)


class Environment:
    """
    Utility class for accessing environment variables with type conversion.

    This class provides convenient methods to read environment variables
    with automatic type conversion and default values.

    Common environment variables in HS-TASKM:
    - TASK_ID: Task execution ID
    - LOG_TYPE: Log type (strategy, plugin, listener)
    - PLUGIN_ENDPOINT: Plugin instance HTTP endpoint
    - LISTENER_ENDPOINT: Listener instance HTTP endpoint

    Example:
        >>> from taskm_sdk import Environment
        >>> task_id = Environment.get("TASK_ID", default=0, type=int)
        >>> log_type = Environment.get("LOG_TYPE", default="strategy")
        >>> enabled = Environment.get("ENABLED", type=bool)
        >>> all_vars = Environment.get_all()
    """

    @staticmethod
    def get(
        name: str,
        default: Optional[T] = None,
        type: Optional[Type[T]] = None
    ) -> Optional[T]:
        """
        Get an environment variable with optional type conversion and default value.

        Args:
            name: Environment variable name
            default: Default value if variable is not set
            type: Type to convert the value to (str, int, float, bool)

        Returns:
            The environment variable value, converted to the specified type,
            or the default value if not set.

        Raises:
            ValueError: If type conversion fails
            TypeError: If an unsupported type is specified

        Example:
            >>> Environment.get("TASK_ID", default=0, type=int)
            123
            >>> Environment.get("LOG_TYPE", default="strategy")
            'strategy'
            >>> Environment.get("ENABLED", type=bool)
            True
            >>> Environment.get("PORT", default=8080, type=int)
            8080
        """
        value = os.getenv(name)

        if value is None:
            return default

        if type is None:
            return value

        # Type conversion
        if type == str:
            return value

        elif type == int:
            try:
                return int(value)
            except ValueError:
                raise ValueError(
                    f"Environment variable '{name}' value '{value}' "
                    f"cannot be converted to int"
                )

        elif type == float:
            try:
                return float(value)
            except ValueError:
                raise ValueError(
                    f"Environment variable '{name}' value '{value}' "
                    f"cannot be converted to float"
                )

        elif type == bool:
            # Handle various boolean representations
            if isinstance(value, bool):
                return value

            normalized = value.strip().lower()
            if normalized in ('true', '1', 'yes', 'on', 't', 'y'):
                return True
            elif normalized in ('false', '0', 'no', 'off', 'f', 'n', ''):
                return False
            else:
                raise ValueError(
                    f"Environment variable '{name}' value '{value}' "
                    f"cannot be converted to bool"
                )

        else:
            raise TypeError(
                f"Unsupported type '{type}'. "
                f"Supported types: str, int, float, bool"
            )

    @staticmethod
    def get_int(name: str, default: Optional[int] = None) -> Optional[int]:
        """
        Get an environment variable as an integer.

        Args:
            name: Environment variable name
            default: Default value if variable is not set

        Returns:
            Integer value or default

        Example:
            >>> Environment.get_int("PORT", 8080)
            8080
            >>> Environment.get_int("TASK_ID")
            123
        """
        return Environment.get(name, default=default, type=int)

    @staticmethod
    def get_float(name: str, default: Optional[float] = None) -> Optional[float]:
        """
        Get an environment variable as a float.

        Args:
            name: Environment variable name
            default: Default value if variable is not set

        Returns:
            Float value or default

        Example:
            >>> Environment.get_float("PRICE", 0.0)
            100.5
        """
        return Environment.get(name, default=default, type=float)

    @staticmethod
    def get_bool(name: str, default: Optional[bool] = None) -> Optional[bool]:
        """
        Get an environment variable as a boolean.

        Accepts: true/false, 1/0, yes/no, on/off, t/f, y/n (case-insensitive)

        Args:
            name: Environment variable name
            default: Default value if variable is not set

        Returns:
            Boolean value or default

        Example:
            >>> Environment.get_bool("ENABLED", False)
            True
            >>> Environment.get_bool("DEBUG")
            False
        """
        return Environment.get(name, default=default, type=bool)

    @staticmethod
    def get_string(name: str, default: Optional[str] = None) -> Optional[str]:
        """
        Get an environment variable as a string.

        Args:
            name: Environment variable name
            default: Default value if variable is not set

        Returns:
            String value or default

        Example:
            >>> Environment.get_string("LOG_TYPE", "strategy")
            'strategy'
        """
        return Environment.get(name, default=default, type=str)

    @staticmethod
    def get_required(name: str, type: Optional[Type[T]] = None) -> T:
        """
        Get a required environment variable.

        Raises an exception if the variable is not set.

        Args:
            name: Environment variable name
            type: Type to convert the value to (str, int, float, bool)

        Returns:
            The environment variable value, converted to the specified type

        Raises:
            ValueError: If the environment variable is not set or type conversion fails

        Example:
            >>> Environment.get_required("TASK_ID", type=int)
            123
            >>> Environment.get_required("API_KEY")
            'secret-key'
        """
        value = os.getenv(name)
        if value is None:
            raise ValueError(f"Required environment variable '{name}' is not set")

        return Environment.get(name, type=type)

    @staticmethod
    def get_all(prefix: Optional[str] = None) -> Dict[str, str]:
        """
        Get all environment variables, optionally filtered by prefix.

        Args:
            prefix: Optional prefix to filter variables (e.g., "TASKM_")

        Returns:
            Dictionary of environment variable names and values

        Example:
            >>> Environment.get_all()
            {'TASK_ID': '123', 'LOG_TYPE': 'strategy', ...}
            >>> Environment.get_all("TASKM_")
            {'TASKM_API_KEY': 'xxx', 'TASKM_TIMEOUT': '30'}
        """
        if prefix:
            return {
                key: value
                for key, value in os.environ.items()
                if key.startswith(prefix)
            }
        return dict(os.environ)

    @staticmethod
    def exists(name: str) -> bool:
        """
        Check if an environment variable is set.

        Args:
            name: Environment variable name

        Returns:
            True if the variable is set, False otherwise

        Example:
            >>> Environment.exists("TASK_ID")
            True
            >>> Environment.exists("NON_EXISTENT")
            False
        """
        return name in os.environ
