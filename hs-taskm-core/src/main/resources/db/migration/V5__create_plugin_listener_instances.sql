-- Create data_plugin_instance table for managing plugin instances
CREATE TABLE data_plugin_instance (
    id BIGSERIAL PRIMARY KEY,
    plugin_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    config JSONB NOT NULL,
    container_id VARCHAR(255),
    status VARCHAR(50) DEFAULT 'STOPPED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_plugin_instance_name UNIQUE (plugin_id, name)
);

-- Create listener_instance table for managing listener instances
CREATE TABLE listener_instance (
    id BIGSERIAL PRIMARY KEY,
    listener_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    config JSONB NOT NULL,
    container_id VARCHAR(255),
    status VARCHAR(50) DEFAULT 'STOPPED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_listener_instance_name UNIQUE (listener_id, name)
);

-- Add new columns to task table
ALTER TABLE task ADD COLUMN plugin_instance_id BIGINT;
ALTER TABLE task ADD COLUMN listener_instance_id BIGINT;
ALTER TABLE task ADD COLUMN plugin_endpoint VARCHAR(500);
ALTER TABLE task ADD COLUMN listener_endpoint VARCHAR(500);

-- Create indexes for better query performance
CREATE INDEX idx_plugin_instance_plugin_id ON data_plugin_instance(plugin_id);
CREATE INDEX idx_plugin_instance_status ON data_plugin_instance(status);
CREATE INDEX idx_listener_instance_listener_id ON listener_instance(listener_id);
CREATE INDEX idx_listener_instance_status ON listener_instance(status);
CREATE INDEX idx_task_plugin_instance_id ON task(plugin_instance_id);
CREATE INDEX idx_task_listener_instance_id ON task(listener_instance_id);

-- Add comments
COMMENT ON TABLE data_plugin_instance IS 'Data plugin instances with specific configurations';
COMMENT ON TABLE listener_instance IS 'Listener instances with specific configurations';

COMMENT ON COLUMN data_plugin_instance.is_default IS 'Whether this instance is the default for the plugin';
COMMENT ON COLUMN listener_instance.is_default IS 'Whether this instance is the default for the listener';

COMMENT ON COLUMN data_plugin_instance.config IS 'Instance configuration values (JSON)';
COMMENT ON COLUMN listener_instance.config IS 'Instance configuration values (JSON)';

COMMENT ON COLUMN data_plugin_instance.status IS 'Instance status: RUNNING, STOPPED';
COMMENT ON COLUMN listener_instance.status IS 'Instance status: RUNNING, STOPPED';

COMMENT ON COLUMN task.plugin_instance_id IS 'Foreign key to the plugin instance used by this task';
COMMENT ON COLUMN task.listener_instance_id IS 'Foreign key to the listener instance used by this task';
COMMENT ON COLUMN task.plugin_endpoint IS 'HTTP endpoint URL for the plugin instance';
COMMENT ON COLUMN task.listener_endpoint IS 'HTTP endpoint URL for the listener instance';
