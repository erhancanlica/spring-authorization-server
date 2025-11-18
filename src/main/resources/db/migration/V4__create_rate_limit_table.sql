-- Create rate limit tracking table
CREATE TABLE rate_limit_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    attempt_count INTEGER DEFAULT 1,
    window_start TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create composite index for efficient lookups
CREATE INDEX idx_rate_limit_lookup ON rate_limit_tracking(identifier, action_type, window_start);

-- Add comments
COMMENT ON COLUMN rate_limit_tracking.identifier IS 'IP address or user_id depending on the rate limit type';
COMMENT ON COLUMN rate_limit_tracking.action_type IS 'Type of action being rate limited (e.g., LOGIN, SMS_OTP, API_REQUEST)';
COMMENT ON COLUMN rate_limit_tracking.window_start IS 'Start of the current rate limit window';
