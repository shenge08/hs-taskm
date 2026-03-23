-- Add container_id column to task table
ALTER TABLE task ADD COLUMN IF NOT EXISTS container_id VARCHAR(255);

COMMENT ON COLUMN task.container_id IS 'Docker container ID';
