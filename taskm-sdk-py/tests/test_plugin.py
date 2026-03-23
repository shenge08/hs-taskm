"""
Unit tests for DataPluginClient.

Tests use responses library to mock HTTP requests.
"""

import pytest
import responses
from requests.exceptions import Timeout, ConnectionError

from taskm_sdk import DataPluginClient
from taskm_sdk.exceptions import (
    PluginConnectionException,
    PluginTimeoutException,
    PluginApiException,
    PluginResponseException,
)


class TestDataPluginClient:
    """Test suite for DataPluginClient."""

    def test_init_with_endpoint(self):
        """Test initialization with explicit endpoint."""
        client = DataPluginClient(endpoint="http://example.com/api")
        assert client.endpoint == "http://example.com/api"
        assert client.timeout == 30

    def test_init_with_env_var(self, monkeypatch):
        """Test initialization reading endpoint from environment variable."""
        monkeypatch.setenv("PLUGIN_ENDPOINT", "http://env-example.com/api")
        client = DataPluginClient()
        assert client.endpoint == "http://env-example.com/api"

    def test_init_without_endpoint_raises_error(self, monkeypatch):
        """Test that initialization fails without endpoint."""
        monkeypatch.delenv("PLUGIN_ENDPOINT", raising=False)
        with pytest.raises(ValueError, match="Endpoint must be provided"):
            DataPluginClient()

    def test_endpoint_trailing_slash_removed(self):
        """Test that trailing slash is removed from endpoint."""
        client = DataPluginClient(endpoint="http://example.com/api/")
        assert client.endpoint == "http://example.com/api"

    @responses.activate
    def test_get_data_success(self):
        """Test successful data retrieval."""
        responses.add(
            responses.POST,
            "http://example.com/api/get-data",
            json={"status": "success", "data": {"symbol": "BTC/USDT", "price": 50000}},
            status=200,
        )

        client = DataPluginClient(endpoint="http://example.com/api")
        data = client.get_data(symbol="BTC/USDT", interval="1h")

        assert data["status"] == "success"
        assert data["data"]["symbol"] == "BTC/USDT"

    @responses.activate
    def test_get_data_timeout(self):
        """Test timeout exception."""
        responses.add(
            responses.POST,
            "http://example.com/api/get-data",
            body=Timeout(),
        )

        client = DataPluginClient(endpoint="http://example.com/api")
        with pytest.raises(PluginTimeoutException) as exc_info:
            client.get_data(symbol="BTC/USDT")

        assert exc_info.value.timeout == 30
        assert "http://example.com/api/get-data" in str(exc_info.value.endpoint)

    @responses.activate
    def test_get_data_connection_error(self):
        """Test connection error exception."""
        responses.add(
            responses.POST,
            "http://example.com/api/get-data",
            body=ConnectionError(),
        )

        client = DataPluginClient(endpoint="http://example.com/api")
        with pytest.raises(PluginConnectionException) as exc_info:
            client.get_data(symbol="BTC/USDT")

        assert "Failed to connect to plugin" in str(exc_info.value)

    @responses.activate
    def test_get_data_http_error(self):
        """Test HTTP error response."""
        responses.add(
            responses.POST,
            "http://example.com/api/get-data",
            json={"error": "Invalid symbol"},
            status=400,
        )

        client = DataPluginClient(endpoint="http://example.com/api")
        with pytest.raises(PluginApiException) as exc_info:
            client.get_data(symbol="INVALID")

        assert exc_info.value.status_code == 400
        assert "Invalid symbol" in exc_info.value.response_body

    @responses.activate
    def test_get_data_api_error_status(self):
        """Test API error status in response."""
        responses.add(
            responses.POST,
            "http://example.com/api/get-data",
            json={"status": "error", "message": "Rate limit exceeded"},
            status=200,
        )

        client = DataPluginClient(endpoint="http://example.com/api")
        with pytest.raises(PluginApiException) as exc_info:
            client.get_data(symbol="BTC/USDT")

        assert "Rate limit exceeded" in str(exc_info.value)

    @responses.activate
    def test_get_data_invalid_json(self):
        """Test invalid JSON response."""
        responses.add(
            responses.POST,
            "http://example.com/api/get-data",
            body="Not JSON",
            status=200,
        )

        client = DataPluginClient(endpoint="http://example.com/api")
        with pytest.raises(PluginResponseException) as exc_info:
            client.get_data(symbol="BTC/USDT")

        assert "Failed to parse JSON response" in str(exc_info.value)

    @responses.activate
    def test_health_check_success(self):
        """Test successful health check."""
        responses.add(
            responses.GET,
            "http://example.com/api/health",
            json={"status": "healthy"},
            status=200,
        )

        client = DataPluginClient(endpoint="http://example.com/api")
        health = client.health_check()

        assert health["status"] == "healthy"

    @responses.activate
    def test_health_check_timeout(self):
        """Test health check timeout."""
        responses.add(
            responses.GET,
            "http://example.com/api/health",
            body=Timeout(),
        )

        client = DataPluginClient(endpoint="http://example.com/api")
        with pytest.raises(PluginTimeoutException):
            client.health_check()

    @responses.activate
    def test_custom_timeout(self):
        """Test custom timeout value."""
        responses.add(
            responses.POST,
            "http://example.com/api/get-data",
            json={"status": "success"},
            status=200,
        )

        client = DataPluginClient(endpoint="http://example.com/api", timeout=60)
        data = client.get_data(symbol="BTC/USDT")

        assert data["status"] == "success"
        assert client.timeout == 60

    def test_context_manager(self):
        """Test using client as context manager."""
        client = DataPluginClient(endpoint="http://example.com/api")
        with client:
            assert client.session is not None
        assert client.session is None  # Session closed after exit
