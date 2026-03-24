"""
TaskM Python SDK

A Python SDK for calling data plugin and listener HTTP APIs in the TaskM system.
"""

from taskm_sdk.plugin import DataPluginClient
from taskm_sdk.listener import ListenerClient
from taskm_sdk.environment import Environment
from taskm_sdk.logger import Logger, get_logger
from taskm_sdk.exceptions import (
    TaskmSdkException,
    PluginException,
    PluginConnectionException,
    PluginTimeoutException,
    PluginApiException,
    PluginResponseException,
    ListenerException,
    ListenerConnectionException,
    ListenerTimeoutException,
    ListenerApiException,
    ListenerResponseException,
)

__version__ = "1.0.0"
__all__ = [
    # Clients
    "DataPluginClient",
    "ListenerClient",
    # Environment
    "Environment",
    # Logger
    "Logger",
    "get_logger",
    # Base exceptions
    "TaskmSdkException",
    "PluginException",
    "ListenerException",
    # Plugin exceptions
    "PluginConnectionException",
    "PluginTimeoutException",
    "PluginApiException",
    "PluginResponseException",
    # Listener exceptions
    "ListenerConnectionException",
    "ListenerTimeoutException",
    "ListenerApiException",
    "ListenerResponseException",
]
