-- Add metadata column to listener table
ALTER TABLE listener ADD COLUMN IF NOT EXISTS metadata JSONB;

COMMENT ON COLUMN listener.metadata IS 'Listener metadata in JSON format';
