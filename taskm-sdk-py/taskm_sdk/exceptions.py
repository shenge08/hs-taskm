"""
Custom exceptions for TaskM SDK.

These exceptions provide detailed error information for plugin and listener API calls.
"""


class TaskmSdkException(Exception):
    """Base exception for all TaskM SDK exceptions."""
    pass


class PluginException(TaskmSdkException):
    """Base exception for plugin-related errors."""
    pass


class PluginConnectionException(PluginException):
    """
    Raised when connection to plugin endpoint fails.

    Attributes:
        message: Error message
        endpoint: The endpoint URL that failed
        original_error: The original exception from requests library
    """

    def __init__(self, message, endpoint=None, original_error=None):
        self.message = message
        self.endpoint = endpoint
        self.original_error = original_error
        super().__init__(self.message)

    def __str__(self):
        if self.endpoint:
            return f"{self.message} (endpoint: {self.endpoint})"
        return self.message


class PluginTimeoutException(PluginException):
    """
    Raised when plugin request times out.

    Attributes:
        message: Error message
        endpoint: The endpoint URL that timed out
        timeout: The timeout value in seconds
    """

    def __init__(self, message, endpoint=None, timeout=None):
        self.message = message
        self.endpoint = endpoint
        self.timeout = timeout
        super().__init__(self.message)

    def __str__(self):
        if self.endpoint and self.timeout:
            return f"{self.message} (endpoint: {self.endpoint}, timeout: {self.timeout}s)"
        return self.message


class PluginApiException(PluginException):
    """
    Raised when plugin API returns an error response.

    Attributes:
        message: Error message
        status_code: HTTP status code
        response_body: Response body from the API
        endpoint: The endpoint URL that returned the error
    """

    def __init__(self, message, status_code=None, response_body=None, endpoint=None):
        self.message = message
        self.status_code = status_code
        self.response_body = response_body
        self.endpoint = endpoint
        super().__init__(self.message)

    def __str__(self):
        parts = [self.message]
        if self.status_code:
            parts.append(f"status_code: {self.status_code}")
        if self.endpoint:
            parts.append(f"endpoint: {self.endpoint}")
        if self.response_body:
            parts.append(f"response: {self.response_body}")
        return " | ".join(parts)


class PluginResponseException(PluginException):
    """
    Raised when plugin response cannot be parsed.

    Attributes:
        message: Error message
        response_text: Raw response text
        endpoint: The endpoint URL that returned invalid response
    """

    def __init__(self, message, response_text=None, endpoint=None):
        self.message = message
        self.response_text = response_text
        self.endpoint = endpoint
        super().__init__(self.message)

    def __str__(self):
        parts = [self.message]
        if self.endpoint:
            parts.append(f"endpoint: {self.endpoint}")
        if self.response_text:
            parts.append(f"response: {self.response_text[:200]}")
        return " | ".join(parts)


class ListenerException(TaskmSdkException):
    """Base exception for listener-related errors."""
    pass


class ListenerConnectionException(ListenerException):
    """
    Raised when connection to listener endpoint fails.

    Attributes:
        message: Error message
        endpoint: The endpoint URL that failed
        original_error: The original exception from requests library
    """

    def __init__(self, message, endpoint=None, original_error=None):
        self.message = message
        self.endpoint = endpoint
        self.original_error = original_error
        super().__init__(self.message)

    def __str__(self):
        if self.endpoint:
            return f"{self.message} (endpoint: {self.endpoint})"
        return self.message


class ListenerTimeoutException(ListenerException):
    """
    Raised when listener request times out.

    Attributes:
        message: Error message
        endpoint: The endpoint URL that timed out
        timeout: The timeout value in seconds
    """

    def __init__(self, message, endpoint=None, timeout=None):
        self.message = message
        self.endpoint = endpoint
        self.timeout = timeout
        super().__init__(self.message)

    def __str__(self):
        if self.endpoint and self.timeout:
            return f"{self.message} (endpoint: {self.endpoint}, timeout: {self.timeout}s)"
        return self.message


class ListenerApiException(ListenerException):
    """
    Raised when listener API returns an error response.

    Attributes:
        message: Error message
        status_code: HTTP status code
        response_body: Response body from the API
        endpoint: The endpoint URL that returned the error
    """

    def __init__(self, message, status_code=None, response_body=None, endpoint=None):
        self.message = message
        self.status_code = status_code
        self.response_body = response_body
        self.endpoint = endpoint
        super().__init__(self.message)

    def __str__(self):
        parts = [self.message]
        if self.status_code:
            parts.append(f"status_code: {self.status_code}")
        if self.endpoint:
            parts.append(f"endpoint: {self.endpoint}")
        if self.response_body:
            parts.append(f"response: {self.response_body}")
        return " | ".join(parts)


class ListenerResponseException(ListenerException):
    """
    Raised when listener response cannot be parsed.

    Attributes:
        message: Error message
        response_text: Raw response text
        endpoint: The endpoint URL that returned invalid response
    """

    def __init__(self, message, response_text=None, endpoint=None):
        self.message = message
        self.response_text = response_text
        self.endpoint = endpoint
        super().__init__(self.message)

    def __str__(self):
        parts = [self.message]
        if self.endpoint:
            parts.append(f"endpoint: {self.endpoint}")
        if self.response_text:
            parts.append(f"response: {self.response_text[:200]}")
        return " | ".join(parts)
