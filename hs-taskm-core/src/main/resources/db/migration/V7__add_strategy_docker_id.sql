ALTER TABLE strategy ADD COLUMN  docker_image_id VARCHAR(255);

COMMENT ON COLUMN strategy.docker_image_id IS 'Custom Docker image ID for strategy execution. If null, uses default image based on
         + language.';