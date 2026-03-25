-- Add docker_image_id column to data_plugin and listener tables
-- This migration adds the docker_image_id field to match the strategy table

-- Add docker_image_id column to data_plugin table
ALTER TABLE data_plugin
ADD COLUMN docker_image_id VARCHAR(255);

-- Add docker_image_id column to listener table
ALTER TABLE listener
ADD COLUMN docker_image_id VARCHAR(255);

-- Add comments for documentation
COMMENT ON COLUMN data_plugin.docker_image_id IS 'Custom Docker image ID for plugin execution. If null, uses default image based on language or image_name.';
COMMENT ON COLUMN listener.docker_image_id IS 'Custom Docker image ID for listener execution. If null, uses default image based on language or image_name.';
