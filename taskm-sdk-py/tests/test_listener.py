"""
Unit tests for ListenerClient.

Tests use responses library to mock HTTP requests.
"""

import pytest
import responses
from requests.exceptions import Timeout, ConnectionError
from datetime import datetime

from taskm_sdk import ListenerClient
from taskm_sdk.exceptions import (
    ListenerConnectionException,
    ListenerTimeoutException,
    ListenerApiException,
)


class TestListenerClient:
    """Test suite for ListenerClient."""

    def test_init_with_parameters(self):
        """Test initialization with explicit parameters."""
        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        assert client.endpoint == "http://example.com/api"
        assert client.task_id == 123
        assert client.timeout == 30

    def test_init_with_env_vars(self, monkeypatch):
        """Test initialization reading from environment variables."""
        monkeypatch.setenv("LISTENER_ENDPOINT", "http://env-example.com/api")
        monkeypatch.setenv("TASK_ID", "456")

        client = ListenerClient()
        assert client.endpoint == "http://env-example.com/api"
        assert client.task_id == 456

    def test_init_without_endpoint_raises_error(self, monkeypatch):
        """Test that initialization fails without endpoint."""
        monkeypatch.delenv("LISTENER_ENDPOINT", raising=False)
        with pytest.raises(ValueError, match="Endpoint must be provided"):
            ListenerClient()

    def test_init_without_task_id_raises_error(self, monkeypatch):
        """Test that initialization fails without task_id."""
        monkeypatch.setenv("LISTENER_ENDPOINT", "http://example.com/api")
        monkeypatch.delenv("TASK_ID", raising=False)
        with pytest.raises(ValueError, match="task_id must be provided"):
            ListenerClient()

    def test_invalid_task_id_env_var(self, monkeypatch):
        """Test that invalid TASK_ID env var raises error."""
        monkeypatch.setenv("LISTENER_ENDPOINT", "http://example.com/api")
        monkeypatch.setenv("TASK_ID", "not-a-number")
        with pytest.raises(ValueError, match="must be an integer"):
            ListenerClient()

    def test_endpoint_trailing_slash_removed(self):
        """Test that trailing slash is removed from endpoint."""
        client = ListenerClient(endpoint="http://example.com/api/", task_id=123)
        assert client.endpoint == "http://example.com/api"

    @responses.activate
    def test_on_task_started_success(self):
        """Test successful task started notification."""
        responses.add(
            responses.POST,
            "http://example.com/api/task-started",
            json={"status": "success", "message": "Task started recorded"},
            status=200,
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        response = client.on_task_started()

        assert response["status"] == "success"
        assert "message" in response

    @responses.activate
    def test_on_task_started_payload(self):
        """Test that task-started payload includes required fields."""
        def request_callback(request):
            import json
            payload = json.loads(request.body)
            assert payload["taskId"] == 123
            assert "timestamp" in payload
            assert payload["timestamp"].endswith("Z")
            return (200, {}, json.dumps({"status": "success"}))

        responses.add_callback(
            responses.POST,
            "http://example.com/api/task-started",
            callback=request_callback,
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        client.on_task_started()

    @responses.activate
    def test_on_task_completed_success(self):
        """Test successful task completed notification."""
        responses.add(
            responses.POST,
            "http://example.com/api/task-completed",
            json={"status": "success", "message": "Task completed recorded"},
            status=200,
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        result = {"profit": 100.5, "trades": 5}
        response = client.on_task_completed(result)

        assert response["status"] == "success"

    @responses.activate
    def test_on_task_completed_payload(self):
        """Test that task-completed payload includes result."""
        def request_callback(request):
            import json
            payload = json.loads(request.body)
            assert payload["taskId"] == 123
            assert "result" in payload
            assert payload["result"]["profit"] == 100.5
            assert "timestamp" in payload
            return (200, {}, json.dumps({"status": "success"}))

        responses.add_callback(
            responses.POST,
            "http://example.com/api/task-completed",
            callback=request_callback,
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        client.on_task_completed(result={"profit": 100.5})

    @responses.activate
    def test_on_task_failed_success(self):
        """Test successful task failed notification."""
        responses.add(
            responses.POST,
            "http://example.com/api/task-failed",
            json={"status": "success", "message": "Task failure recorded"},
            status=200,
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        response = client.on_task_failed(error="Insufficient balance")

        assert response["status"] == "success"

    @responses.activate
    def test_on_task_failed_payload(self):
        """Test that task-failed payload includes error."""
        def request_callback(request):
            import json
            payload = json.loads(request.body)
            assert payload["taskId"] == 123
            assert "error" in payload
            assert payload["error"] == "Insufficient balance"
            assert "timestamp" in payload
            return (200, {}, json.dumps({"status": "success"}))

        responses.add_callback(
            responses.POST,
            "http://example.com/api/task-failed",
            callback=request_callback,
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        client.on_task_failed(error="Insufficient balance")

    @responses.activate
    def test_timeout_exception(self):
        """Test timeout exception."""
        responses.add(
            responses.POST,
            "http://example.com/api/task-started",
            body=Timeout(),
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        with pytest.raises(ListenerTimeoutException) as exc_info:
            client.on_task_started()

        assert exc_info.value.timeout == 30

    @responses.activate
    def test_connection_error_exception(self):
        """Test connection error exception."""
        responses.add(
            responses.POST,
            "http://example.com/api/task-started",
            body=ConnectionError(),
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        with pytest.raises(ListenerConnectionException) as exc_info:
            client.on_task_started()

        assert "Failed to connect to listener" in str(exc_info.value)

    @responses.activate
    def test_http_error_exception(self):
        """Test HTTP error response."""
        responses.add(
            responses.POST,
            "http://example.com/api/task-completed",
            json={"error": "Invalid task ID"},
            status=400,
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        with pytest.raises(ListenerApiException) as exc_info:
            client.on_task_completed(result={})

        assert exc_info.value.status_code == 400

    @responses.activate
    def test_invalid_json_response(self):
        """Test invalid JSON response."""
        responses.add(
            responses.POST,
            "http://example.com/api/task-failed",
            body="Not JSON",
            status=200,
        )

        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        with pytest.raises(ListenerApiException) as exc_info:
            client.on_task_failed(error="Error")

        assert "Failed to parse JSON response" in str(exc_info.value)

    def test_context_manager(self):
        """Test using client as context manager."""
        client = ListenerClient(endpoint="http://example.com/api", task_id=123)
        with client:
            assert client.session is not None
        assert client.session is None  # Session closed after exit
