"""
ListenerClient for calling listener HTTP APIs.

This module provides a Python client for interacting with listener instances
that expose HTTP endpoints. It handles connection management, timeout control,
and error handling.
"""

import os
from typing import Dict, Any, Optional
from datetime import datetime
import requests

from taskm_sdk.exceptions import (
    ListenerConnectionException,
    ListenerTimeoutException,
    ListenerApiException,
)


class ListenerClient:
    """
    Client for calling listener HTTP APIs.

    This client provides methods to notify listeners about task lifecycle events.
    It automatically reads the endpoint and task_id from environment variables if not provided.

    Attributes:
        endpoint: The base URL of the listener instance API (e.g., http://listener-1:8080/instances/prod/api)
        task_id: The task ID for this execution
        timeout: Request timeout in seconds (default: 30)
        session: Requests session for connection pooling
    """

    DEFAULT_TIMEOUT = 30
    ENV_ENDPOINT = "LISTENER_ENDPOINT"
    ENV_TASK_ID = "TASK_ID"

    def __init__(self, endpoint: Optional[str] = None, task_id: Optional[int] = None, timeout: int = DEFAULT_TIMEOUT):
        """
        Initialize the ListenerClient.

        Args:
            endpoint: The base URL of the listener instance API.
                     If None, reads from LISTENER_ENDPOINT environment variable.
            task_id: The task ID. If None, reads from TASK_ID environment variable.
            timeout: Request timeout in seconds. Defaults to 30.

        Raises:
            ValueError: If endpoint or task_id is not provided and env vars are not set.
        """
        if endpoint is None:
            endpoint = os.getenv(self.ENV_ENDPOINT)

        if not endpoint:
            raise ValueError(
                f"Endpoint must be provided either as parameter or "
                f"through {self.ENV_ENDPOINT} environment variable"
            )

        if task_id is None:
            task_id_str = os.getenv(self.ENV_TASK_ID)
            if task_id_str:
                try:
                    task_id = int(task_id_str)
                except ValueError:
                    raise ValueError(f"{self.ENV_TASK_ID} environment variable must be an integer")

        if task_id is None:
            raise ValueError(
                f"task_id must be provided either as parameter or "
                f"through {self.ENV_TASK_ID} environment variable"
            )

        # Ensure endpoint doesn't end with /
        self.endpoint = endpoint.rstrip("/")
        self.task_id = task_id
        self.timeout = timeout
        self.session = requests.Session()

    def on_task_started(self) -> Dict[str, Any]:
        """
        Notify the listener that the task has started.

        Returns:
            Dictionary containing the listener's response.
            Format: {"status": "success", "message": "..."}

        Raises:
            ListenerConnectionException: If connection to listener fails.
            ListenerTimeoutException: If request times out.
            ListenerApiException: If listener returns an error response.

        Example:
            >>> listener = ListenerClient()
            >>> listener.on_task_started()
        """
        return self._send_event("task-started", {})

    def on_task_completed(self, result: Dict[str, Any]) -> Dict[str, Any]:
        """
        Notify the listener that the task has completed successfully.

        Args:
            result: Task execution result (e.g., {"profit": 100.5, "trades": 5})

        Returns:
            Dictionary containing the listener's response.
            Format: {"status": "success", "message": "..."}

        Raises:
            ListenerConnectionException: If connection to listener fails.
            ListenerTimeoutException: If request times out.
            ListenerApiException: If listener returns an error response.

        Example:
            >>> listener = ListenerClient()
            >>> result = {"profit": 100.5, "trades": 5}
            >>> listener.on_task_completed(result)
        """
        return self._send_event("task-completed", {"result": result})

    def on_task_failed(self, error: str) -> Dict[str, Any]:
        """
        Notify the listener that the task has failed.

        Args:
            error: Error message describing the failure

        Returns:
            Dictionary containing the listener's response.
            Format: {"status": "success", "message": "..."}

        Raises:
            ListenerConnectionException: If connection to listener fails.
            ListenerTimeoutException: If request times out.
            ListenerApiException: If listener returns an error response.

        Example:
            >>> listener = ListenerClient()
            >>> listener.on_task_failed("Insufficient balance")
        """
        return self._send_event("task-failed", {"error": error})

    def _send_event(self, event_type: str, extra_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Internal method to send an event to the listener.

        Args:
            event_type: Type of event (e.g., "task-started", "task-completed", "task-failed")
            extra_data: Additional data to include in the request

        Returns:
            Dictionary containing the listener's response.

        Raises:
            ListenerConnectionException: If connection to listener fails.
            ListenerTimeoutException: If request times out.
            ListenerApiException: If listener returns an error response.
        """
        url = f"{self.endpoint}/{event_type}"

        # Build request payload
        payload = {
            "taskId": self.task_id,
            "timestamp": datetime.utcnow().isoformat() + "Z",
            **extra_data
        }

        try:
            response = self.session.post(
                url,
                json=payload,
                timeout=self.timeout
            )
            response.raise_for_status()

        except requests.exceptions.Timeout as e:
            raise ListenerTimeoutException(
                f"Request timed out after {self.timeout}s",
                endpoint=url,
                timeout=self.timeout
            ) from e

        except requests.exceptions.ConnectionError as e:
            raise ListenerConnectionException(
                f"Failed to connect to listener",
                endpoint=url,
                original_error=e
            ) from e

        except requests.exceptions.HTTPError as e:
            raise ListenerApiException(
                f"Listener API returned error: {e.response.reason}",
                status_code=e.response.status_code,
                response_body=e.response.text,
                endpoint=url
            ) from e

        except requests.exceptions.RequestException as e:
            raise ListenerConnectionException(
                f"Request failed: {str(e)}",
                endpoint=url,
                original_error=e
            ) from e

        # Parse response
        try:
            return response.json()
        except ValueError as e:
            raise ListenerApiException(
                f"Failed to parse JSON response",
                status_code=response.status_code,
                response_body=response.text,
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
