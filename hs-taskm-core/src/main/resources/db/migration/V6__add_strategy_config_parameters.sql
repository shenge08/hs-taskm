-- Add configuration parameters to strategy table
-- This migration adds JSONB fields for strategy configuration parameters

-- Add config_parameters column for parameter definitions
ALTER TABLE strategy
ADD COLUMN  config_parameters JSONB;


-- Add parameter_defaults column for default parameter values
ALTER TABLE strategy
ADD COLUMN  parameter_defaults JSONB;

-- Add comments for documentation
COMMENT ON COLUMN strategy.config_parameters IS 'Strategy configuration parameter definitions in JSON format';
COMMENT ON COLUMN strategy.parameter_defaults IS 'Default values for strategy parameters in JSON format';

-- Create index for better query performance on config_parameters
CREATE INDEX  idx_strategy_config_parameters ON strategy USING GIN (config_parameters);

-- Create index for better query performance on parameter_defaults
CREATE INDEX  idx_strategy_parameter_defaults ON strategy USING GIN (parameter_defaults);
