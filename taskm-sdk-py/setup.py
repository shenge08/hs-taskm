"""
Setup configuration for taskm-sdk-py package.
"""

from setuptools import setup, find_packages
import os

# Read the README file for long description
def read_file(filename):
    """Read file contents."""
    here = os.path.abspath(os.path.dirname(__file__))
    with open(os.path.join(here, filename), encoding='utf-8') as f:
        return f.read()

setup(
    name="taskm-sdk-py",
    version="1.0.0",
    author="TaskM Team",
    author_email="info@taskm.io",
    description="Python SDK for TaskM Plugin and Listener APIs",
    long_description=read_file("README.md"),
    long_description_content_type="text/markdown",
    url="https://github.com/taskm/taskm-sdk-py",
    packages=find_packages(exclude=["tests", "tests.*"]),
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "Topic :: Software Development :: Libraries :: Python Modules",
        "License :: OSI Approved :: MIT License",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
    ],
    keywords="taskm sdk plugin listener api",
    python_requires=">=3.7",
    install_requires=[
        "requests>=2.25.0",
    ],
    extras_require={
        "dev": [
            "pytest>=6.0",
            "pytest-cov>=2.10",
            "responses>=0.18",
            "black>=20.8b1",
            "flake8>=3.8.0",
            "mypy>=0.900",
        ],
    },
    project_urls={
        "Bug Reports": "https://github.com/taskm/taskm-sdk-py/issues",
        "Source": "https://github.com/taskm/taskm-sdk-py",
    },
)
