-- HS-TASKM Database Schema
-- This file contains the initial database schema for HS-TASKM

-- Strategy table: Stores trading strategy definitions
CREATE TABLE IF NOT EXISTS strategy (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE strategy IS 'Trading strategy definitions';
COMMENT ON COLUMN strategy.name IS 'Unique strategy name';
COMMENT ON COLUMN strategy.language IS 'Programming language (python, javascript, etc.)';
COMMENT ON COLUMN strategy.code IS 'Strategy code to be executed';

-- Task table: Stores task execution records
CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL REFERENCES strategy(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    parameters JSONB,
    result JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

COMMENT ON TABLE task IS 'Task execution records';
COMMENT ON COLUMN task.status IS 'Task status: PENDING, RUNNING, COMPLETED, FAILED';
COMMENT ON COLUMN task.parameters IS 'Task execution parameters in JSON format';
COMMENT ON COLUMN task.result IS 'Task execution result in JSON format';

-- Container execution table: Stores container execution records
CREATE TABLE IF NOT EXISTS container_execution (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    container_id VARCHAR(255),
    image_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    exit_code INTEGER,
    log_output TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP
);

COMMENT ON TABLE container_execution IS 'Container execution records';
COMMENT ON COLUMN container_execution.container_id IS 'Docker container ID';
COMMENT ON COLUMN container_execution.image_name IS 'Docker image used for execution';
COMMENT ON COLUMN container_execution.status IS 'Container status: CREATED, RUNNING, STOPPED, FAILED';

-- Data plugin table: Stores data plugin configurations
CREATE TABLE IF NOT EXISTS data_plugin (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    plugin_type VARCHAR(50) NOT NULL,
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    config_parameters JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE data_plugin IS 'Data plugin configurations';
COMMENT ON COLUMN data_plugin.plugin_type IS 'Plugin type: SOURCE, TRANSFORM, SINK';
COMMENT ON COLUMN data_plugin.config_parameters IS 'Configurable parameters for testing';
COMMENT ON COLUMN data_plugin.metadata IS 'Plugin metadata in JSON format';

-- Listener table: Stores listener configurations
CREATE TABLE IF NOT EXISTS listener (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    event_type VARCHAR(100) NOT NULL,
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    parameters JSONB,
    parameter_defaults JSONB,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE listener IS 'Event listener configurations';
COMMENT ON COLUMN listener.event_type IS 'Event type to listen for';
COMMENT ON COLUMN listener.parameters IS 'Listener parameters in JSON format';
COMMENT ON COLUMN listener.parameter_defaults IS 'Default values for parameters';

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_task_strategy_id ON task(strategy_id);
CREATE INDEX IF NOT EXISTS idx_task_status ON task(status);
CREATE INDEX IF NOT EXISTS idx_task_created_at ON task(created_at);

CREATE INDEX IF NOT EXISTS idx_container_execution_task_id ON container_execution(task_id);
CREATE INDEX IF NOT EXISTS idx_container_execution_status ON container_execution(status);

CREATE INDEX IF NOT EXISTS idx_data_plugin_type ON data_plugin(plugin_type);

CREATE INDEX IF NOT EXISTS idx_listener_event_type ON listener(event_type);
CREATE INDEX IF NOT EXISTS idx_listener_enabled ON listener(enabled);
