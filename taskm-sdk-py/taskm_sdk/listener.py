"""
ListenerClient for notifying listeners about task lifecycle events.

This module provides a simple client for sending event notifications to listeners.
It uses fire-and-forget mode - sends events without waiting for responses.
"""

import os
from typing import Dict, Any, Optional
from datetime import datetime
import requests

from taskm_sdk.exceptions import (
    ListenerConnectionException,
    ListenerTimeoutException,
)


class ListenerClient:
    """
    Client for sending event notifications to listeners.

    This client provides simple methods to notify listeners about task lifecycle events.
    It uses fire-and-forget mode - events are sent without waiting for responses.

    The listener container runs an HTTP server that receives these events.

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

    def on_task_started(self):
        """
        Notify the listener that the task has started.

        This sends an event notification without waiting for response.

        Raises:
            ListenerConnectionException: If connection to listener fails.
            ListenerTimeoutException: If request times out.

        Example:
            >>> listener = ListenerClient()
            >>> listener.on_task_started()
        """
        self._send_event("task-started", {})

    def on_task_completed(self, result: Dict[str, Any]):
        """
        Notify the listener that the task has completed successfully.

        This sends an event notification without waiting for response.

        Args:
            result: Task execution result (e.g., {"profit": 100.5, "trades": 5})

        Raises:
            ListenerConnectionException: If connection to listener fails.
            ListenerTimeoutException: If request times out.

        Example:
            >>> listener = ListenerClient()
            >>> result = {"profit": 100.5, "trades": 5}
            >>> listener.on_task_completed(result)
        """
        self._send_event("task-completed", {"result": result})

    def on_task_failed(self, error: str):
        """
        Notify the listener that the task has failed.

        This sends an event notification without waiting for response.

        Args:
            error: Error message describing the failure

        Raises:
            ListenerConnectionException: If connection to listener fails.
            ListenerTimeoutException: If request times out.

        Example:
            >>> listener = ListenerClient()
            >>> listener.on_task_failed("Insufficient balance")
        """
        self._send_event("task-failed", {"error": error})

    def _send_event(self, event_type: str, extra_data: Dict[str, Any]):
        """
        Internal method to send an event to the listener.

        This uses fire-and-forget mode - sends the event and immediately returns.

        Args:
            event_type: Type of event (e.g., "task-started", "task-completed", "task-failed")
            extra_data: Additional data to include in the request

        Raises:
            ListenerConnectionException: If connection to listener fails.
            ListenerTimeoutException: If request times out.
        """
        url = f"{self.endpoint}/{event_type}"

        # Build request payload
        payload = {
            "taskId": self.task_id,
            "timestamp": datetime.utcnow().isoformat() + "Z",
            **extra_data
        }

        try:
            # Fire and forget - don't wait for response
            self.session.post(
                url,
                json=payload,
                timeout=self.timeout
            )
        except requests.exceptions.Timeout as e:
            # Still throw exception so caller knows notification failed
            raise ListenerTimeoutException(
                f"Request timed out after {self.timeout}s",
                endpoint=url,
                timeout=self.timeout
            ) from e

        except (requests.exceptions.ConnectionError, requests.exceptions.RequestException) as e:
            # Connection failed - throw exception
            raise ListenerConnectionException(
                f"Failed to send event: {str(e)}",
                endpoint=url,
                original_error=e
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
