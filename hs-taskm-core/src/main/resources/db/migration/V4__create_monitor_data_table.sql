-- Create monitor_data table for storing container resource usage metrics
CREATE TABLE IF NOT EXISTS monitor_data (
    id BIGSERIAL PRIMARY KEY,
    container_id VARCHAR(255) NOT NULL,
    task_id BIGINT NOT NULL,
    cpu_usage DOUBLE PRECISION,
    memory_usage BIGINT,
    memory_limit BIGINT,
    memory_usage_percent DOUBLE PRECISION,
    network_rx_bytes BIGINT,
    network_tx_bytes BIGINT,
    block_read_bytes BIGINT,
    block_write_bytes BIGINT,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX  idx_monitor_data_container_id ON monitor_data(container_id);
CREATE INDEX  idx_monitor_data_task_id ON monitor_data(task_id);
CREATE INDEX  idx_monitor_data_timestamp ON monitor_data(timestamp);
CREATE INDEX  idx_monitor_data_container_timestamp ON monitor_data(container_id, timestamp);

-- Add comments
COMMENT ON TABLE monitor_data IS 'Container resource usage metrics';
COMMENT ON COLUMN monitor_data.container_id IS 'Docker container ID being monitored';
COMMENT ON COLUMN monitor_data.task_id IS 'Task ID associated with this container';
COMMENT ON COLUMN monitor_data.cpu_usage IS 'CPU usage percentage (0-100)';
COMMENT ON COLUMN monitor_data.memory_usage IS 'Memory usage in bytes';
COMMENT ON COLUMN monitor_data.memory_limit IS 'Memory limit in bytes';
COMMENT ON COLUMN monitor_data.memory_usage_percent IS 'Memory usage percentage (0-100)';
COMMENT ON COLUMN monitor_data.network_rx_bytes IS 'Network RX bytes (total bytes received)';
COMMENT ON COLUMN monitor_data.network_tx_bytes IS 'Network TX bytes (total bytes sent)';
COMMENT ON COLUMN monitor_data.block_read_bytes IS 'Block I/O read bytes (total bytes read)';
COMMENT ON COLUMN monitor_data.block_write_bytes IS 'Block I/O write bytes (total bytes written)';
COMMENT ON COLUMN monitor_data.timestamp IS 'Collection timestamp';
