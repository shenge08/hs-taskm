"""
DataPluginClient for calling data plugin HTTP APIs.

This module provides a Python client for interacting with data plugin instances
that expose HTTP endpoints. It handles connection management, timeout control,
and error handling.
"""

import os
from typing import Dict, Any, Optional
import requests

from taskm_sdk.exceptions import (
    PluginConnectionException,
    PluginTimeoutException,
    PluginApiException,
    PluginResponseException,
)


class DataPluginClient:
    """
    Client for calling data plugin HTTP APIs.

    This client provides methods to fetch data from plugin instances.
    It automatically reads the endpoint from environment variable if not provided.

    Attributes:
        endpoint: The base URL of the plugin instance API (e.g., http://plugin-1:8080/instances/prod/api)
        timeout: Request timeout in seconds (default: 30)
        session: Requests session for connection pooling
    """

    DEFAULT_TIMEOUT = 30
    ENV_ENDPOINT = "PLUGIN_ENDPOINT"

    def __init__(self, endpoint: Optional[str] = None, timeout: int = DEFAULT_TIMEOUT):
        """
        Initialize the DataPluginClient.

        Args:
            endpoint: The base URL of the plugin instance API.
                     If None, reads from PLUGIN_ENDPOINT environment variable.
            timeout: Request timeout in seconds. Defaults to 30.

        Raises:
            ValueError: If endpoint is not provided and PLUGIN_ENDPOINT env var is not set.
        """
        if endpoint is None:
            endpoint = os.getenv(self.ENV_ENDPOINT)

        if not endpoint:
            raise ValueError(
                f"Endpoint must be provided either as parameter or "
                f"through {self.ENV_ENDPOINT} environment variable"
            )

        # Ensure endpoint doesn't end with /
        self.endpoint = endpoint.rstrip("/")
        self.timeout = timeout
        self.session = requests.Session()

    def get_data(self, **params) -> Dict[str, Any]:
        """
        Fetch data from the plugin.

        This method calls the plugin's /get-data endpoint with the provided parameters.

        Args:
            **params: Arbitrary keyword arguments to pass as request parameters.
                     Common parameters include: symbol, interval, apiKey, etc.

        Returns:
            Dictionary containing the plugin's response data.
            Format: {"status": "success", "data": {...}}

        Raises:
            PluginConnectionException: If connection to plugin fails.
            PluginTimeoutException: If request times out.
            PluginApiException: If plugin returns an error response.
            PluginResponseException: If response cannot be parsed.

        Example:
            >>> plugin = DataPluginClient()
            >>> data = plugin.get_data(symbol="BTC/USDT", interval="1h")
            >>> print(data["data"])
        """
        url = f"{self.endpoint}/get-data"

        try:
            response = self.session.post(
                url,
                json=params,
                timeout=self.timeout
            )
            response.raise_for_status()

        except requests.exceptions.Timeout as e:
            raise PluginTimeoutException(
                f"Request timed out after {self.timeout}s",
                endpoint=url,
                timeout=self.timeout
            ) from e

        except requests.exceptions.ConnectionError as e:
            raise PluginConnectionException(
                f"Failed to connect to plugin",
                endpoint=url,
                original_error=e
            ) from e

        except requests.exceptions.HTTPError as e:
            raise PluginApiException(
                f"Plugin API returned error: {e.response.reason}",
                status_code=e.response.status_code,
                response_body=e.response.text,
                endpoint=url
            ) from e

        except requests.exceptions.RequestException as e:
            raise PluginConnectionException(
                f"Request failed: {str(e)}",
                endpoint=url,
                original_error=e
            ) from e

        # Parse response
        try:
            data = response.json()

            # Check for API-level errors
            if isinstance(data, dict) and data.get("status") == "error":
                raise PluginApiException(
                    data.get("message", "Plugin returned error status"),
                    status_code=response.status_code,
                    response_body=data,
                    endpoint=url
                )

            return data

        except ValueError as e:
            raise PluginResponseException(
                f"Failed to parse JSON response",
                response_text=response.text,
                endpoint=url
            ) from e

    def health_check(self) -> Dict[str, Any]:
        """
        Check if the plugin is healthy.

        Returns:
            Dictionary containing health status.
            Format: {"status": "healthy"}

        Raises:
            PluginConnectionException: If connection to plugin fails.
            PluginTimeoutException: If request times out.

        Example:
            >>> plugin = DataPluginClient()
            >>> health = plugin.health_check()
            >>> print(health["status"])
            healthy
        """
        url = f"{self.endpoint}/health"

        try:
            response = self.session.get(url, timeout=self.timeout)
            response.raise_for_status()
            return response.json()

        except requests.exceptions.Timeout as e:
            raise PluginTimeoutException(
                f"Health check timed out after {self.timeout}s",
                endpoint=url,
                timeout=self.timeout
            ) from e

        except requests.exceptions.ConnectionError as e:
            raise PluginConnectionException(
                f"Failed to connect to plugin for health check",
                endpoint=url,
                original_error=e
            ) from e

        except (requests.exceptions.HTTPError, ValueError) as e:
            raise PluginApiException(
                f"Health check failed: {str(e)}",
                status_code=response.status_code if hasattr(e, 'response') else None,
                endpoint=url
            ) from e

    def close(self):
        """Close the requests session."""
        self.session.close()

    def __enter__(self):
        """Context manager entry."""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        self.close()
