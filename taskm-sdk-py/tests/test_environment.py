"""
Unit tests for Environment utility class.
"""

import os
import unittest
from taskm_sdk import Environment


class TestEnvironment(unittest.TestCase):
    """Test cases for Environment class."""

    def setUp(self):
        """Set up test environment variables."""
        # Save original environment
        self.original_env = os.environ.copy()

        # Set test environment variables
        os.environ["TASK_ID"] = "123"
        os.environ["LOG_TYPE"] = "strategy"
        os.environ["PORT"] = "8080"
        os.environ["PRICE"] = "100.5"
        os.environ["ENABLED"] = "true"
        os.environ["DISABLED"] = "false"
        os.environ["FLAG"] = "1"
        os.environ["TASKM_API_KEY"] = "secret-key"
        os.environ["TASKM_TIMEOUT"] = "30"

    def tearDown(self):
        """Restore original environment."""
        os.environ.clear()
        os.environ.update(self.original_env)

    def test_get_string(self):
        """Test getting string environment variable."""
        self.assertEqual(Environment.get_string("LOG_TYPE"), "strategy")
        self.assertEqual(Environment.get_string("NON_EXISTENT", "default"), "default")

    def test_get_int(self):
        """Test getting integer environment variable."""
        self.assertEqual(Environment.get_int("TASK_ID"), 123)
        self.assertEqual(Environment.get_int("PORT"), 8080)
        self.assertEqual(Environment.get_int("NON_EXISTENT", 0), 0)

    def test_get_float(self):
        """Test getting float environment variable."""
        self.assertEqual(Environment.get_float("PRICE"), 100.5)
        self.assertEqual(Environment.get_float("NON_EXISTENT", 0.0), 0.0)

    def test_get_bool(self):
        """Test getting boolean environment variable."""
        self.assertTrue(Environment.get_bool("ENABLED"))
        self.assertFalse(Environment.get_bool("DISABLED"))
        self.assertTrue(Environment.get_bool("FLAG"))
        self.assertIsNone(Environment.get_bool("NON_EXISTENT"))
        self.assertFalse(Environment.get_bool("NON_EXISTENT", False))

    def test_get_with_type_conversion(self):
        """Test get method with type parameter."""
        self.assertEqual(Environment.get("TASK_ID", type=int), 123)
        self.assertEqual(Environment.get("LOG_TYPE", type=str), "strategy")
        self.assertEqual(Environment.get("PRICE", type=float), 100.5)
        self.assertTrue(Environment.get("ENABLED", type=bool))

    def test_get_required(self):
        """Test get_required method."""
        self.assertEqual(Environment.get_required("TASK_ID", type=int), 123)
        self.assertEqual(Environment.get_required("LOG_TYPE"), "strategy")

        with self.assertRaises(ValueError):
            Environment.get_required("NON_EXISTENT")

    def test_get_all(self):
        """Test get_all method."""
        all_vars = Environment.get_all()
        self.assertIn("TASK_ID", all_vars)
        self.assertIn("LOG_TYPE", all_vars)

        taskm_vars = Environment.get_all("TASKM_")
        self.assertIn("TASKM_API_KEY", taskm_vars)
        self.assertIn("TASKM_TIMEOUT", taskm_vars)
        self.assertNotIn("LOG_TYPE", taskm_vars)

    def test_exists(self):
        """Test exists method."""
        self.assertTrue(Environment.exists("TASK_ID"))
        self.assertFalse(Environment.exists("NON_EXISTENT"))

    def test_invalid_type_conversion(self):
        """Test invalid type conversion raises error."""
        with self.assertRaises(ValueError):
            Environment.get_int("LOG_TYPE")  # "strategy" is not an int

        with self.assertRaises(ValueError):
            Environment.get_bool("LOG_TYPE")  # "strategy" is not a bool

    def test_unsupported_type(self):
        """Test unsupported type raises error."""
        with self.assertRaises(TypeError):
            Environment.get("TASK_ID", type=list)

    def test_bool_various_formats(self):
        """Test boolean conversion with various formats."""
        test_cases = [
            ("true", True),
            ("True", True),
            ("TRUE", True),
            ("1", True),
            ("yes", True),
            ("YES", True),
            ("on", True),
            ("false", False),
            ("False", False),
            ("0", False),
            ("no", False),
            ("NO", False),
            ("off", False),
            ("", False),
        ]

        for value, expected in test_cases:
            os.environ["TEST_BOOL"] = value
            self.assertEqual(Environment.get_bool("TEST_BOOL"), expected)

    def test_default_values(self):
        """Test default values."""
        self.assertEqual(Environment.get("NON_EXISTENT", "default"), "default")
        self.assertEqual(Environment.get_int("NON_EXISTENT", 42), 42)
        self.assertEqual(Environment.get_float("NON_EXISTENT", 3.14), 3.14)
        self.assertTrue(Environment.get_bool("NON_EXISTENT", True))


if __name__ == "__main__":
    unittest.main()
