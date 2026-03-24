-- Add image_name column to data_plugin and listener tables
-- This migration adds the image_name field that was missing from the initial schema

-- Add image_name column to data_plugin table
ALTER TABLE data_plugin
ADD COLUMN IF NOT EXISTS image_name VARCHAR(255);

-- Add image_name column to listener table
ALTER TABLE listener
ADD COLUMN IF NOT EXISTS image_name VARCHAR(255);

-- Add comments for documentation
COMMENT ON COLUMN data_plugin.image_name IS 'Docker image name for plugin execution. If null, uses default image based on language.';
COMMENT ON COLUMN listener.image_name IS 'Docker image name for listener execution. If null, uses default image based on language.';
