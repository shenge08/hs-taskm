"""
Logger utility for HS-TASKM SDK.

This module provides a simple logging utility that automatically configures
logging based on environment variables and provides convenient logging methods.
"""

import os
import sys
import logging
import json
from datetime import datetime
from typing import Any, Optional, Dict


class Logger:
    """
    Simple logger for TaskM strategy execution.

    This logger provides convenient logging methods with automatic configuration
    from environment variables. It supports both plain text and JSON logging.

    Environment variables:
    - LOG_LEVEL: Logging level (DEBUG, INFO, WARNING, ERROR, default: INFO)
    - LOG_FORMAT: Log format (text, json, default: text)
    - LOG_TIME_FORMAT: Time format for text logs (default: %Y-%m-%d %H:%M:%S)

    Example:
        >>> from taskm_sdk import Logger
        >>> logger = Logger("MyStrategy")
        >>> logger.info("Strategy started")
        >>> logger.debug("Processing data", symbol="BTC/USDT")
        >>> logger.error("Failed to execute", error=str(e))
    """

    # Log level mapping
    LEVELS = {
        'DEBUG': logging.DEBUG,
        'INFO': logging.INFO,
        'WARNING': logging.WARNING,
        'ERROR': logging.ERROR,
        'CRITICAL': logging.CRITICAL,
    }

    def __init__(
        self,
        name: str,
        level: Optional[str] = None,
        format_type: Optional[str] = None,
        handler: Optional[logging.Handler] = None
    ):
        """
        Initialize the logger.

        Args:
            name: Logger name (usually strategy or component name)
            level: Log level (DEBUG, INFO, WARNING, ERROR). If None, reads from LOG_LEVEL env var
            format_type: Log format type ('text' or 'json'). If None, reads from LOG_FORMAT env var
            handler: Custom logging handler. If None, logs to stdout
        """
        self.name = name
        self.logger = logging.getLogger(name)
        self.logger.setLevel(logging.DEBUG)

        # Determine log level from parameter or environment
        if level is None:
            level = os.getenv('LOG_LEVEL', 'INFO').upper()
        self.level = self.LEVELS.get(level, logging.INFO)

        # Determine format type from parameter or environment
        if format_type is None:
            format_type = os.getenv('LOG_FORMAT', 'text').lower()
        self.format_type = format_type

        # Clear existing handlers to avoid duplicates
        self.logger.handlers.clear()

        # Create handler
        if handler is None:
            handler = logging.StreamHandler(sys.stdout)

        handler.setLevel(self.level)

        # Set formatter
        if self.format_type == 'json':
            formatter = JsonFormatter(name)
        else:
            time_format = os.getenv('LOG_TIME_FORMAT', '%Y-%m-%d %H:%M:%S')
            formatter = logging.Formatter(
                fmt='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                datefmt=time_format
            )

        handler.setFormatter(formatter)
        self.logger.addHandler(handler)

        # Prevent propagation to avoid duplicate logs
        self.logger.propagate = False

    def debug(self, message: str, **kwargs):
        """Log debug message."""
        extra = kwargs if kwargs else {}
        if self.format_type == 'json':
            self.logger.debug(message, extra={'data': extra})
        else:
            self.logger.debug(f"{message} {extra if extra else ''}".strip())

    def info(self, message: str, **kwargs):
        """Log info message."""
        extra = kwargs if kwargs else {}
        if self.format_type == 'json':
            self.logger.info(message, extra={'data': extra})
        else:
            self.logger.info(f"{message} {extra if extra else ''}".strip())

    def warning(self, message: str, **kwargs):
        """Log warning message."""
        extra = kwargs if kwargs else {}
        if self.format_type == 'json':
            self.logger.warning(message, extra={'data': extra})
        else:
            self.logger.warning(f"{message} {extra if extra else ''}".strip())

    def error(self, message: str, **kwargs):
        """Log error message."""
        extra = kwargs if kwargs else {}
        if self.format_type == 'json':
            self.logger.error(message, extra={'data': extra})
        else:
            self.logger.error(f"{message} {extra if extra else ''}".strip())

    def critical(self, message: str, **kwargs):
        """Log critical message."""
        extra = kwargs if kwargs else {}
        if self.format_type == 'json':
            self.logger.critical(message, extra={'data': extra})
        else:
            self.logger.critical(f"{message} {extra if extra else ''}".strip())


class JsonFormatter(logging.Formatter):
    """JSON formatter for structured logging."""

    def __init__(self, logger_name: str):
        super().__init__()
        self.logger_name = logger_name

    def format(self, record: logging.LogRecord) -> str:
        """Format log record as JSON."""
        log_data = {
            'timestamp': datetime.utcnow().isoformat() + 'Z',
            'logger': self.logger_name,
            'level': record.levelname,
            'message': record.getMessage(),
        }

        # Add extra data if present
        if hasattr(record, 'data') and record.data:
            log_data['data'] = record.data

        # Add exception info if present
        if record.exc_info:
            log_data['exception'] = self.formatException(record.exc_info)

        return json.dumps(log_data)


# Convenience function for quick logger creation
def get_logger(name: str, **kwargs) -> Logger:
    """
    Get or create a logger with the given name.

    Args:
        name: Logger name
        **kwargs: Additional arguments passed to Logger constructor

    Returns:
        Logger instance

    Example:
        >>> logger = get_logger("MyStrategy")
        >>> logger.info("Starting")
    """
    return Logger(name, **kwargs)
