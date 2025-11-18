-- Create verification tokens table
CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_verification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_verification_token ON verification_tokens(token);
CREATE INDEX idx_verification_user_id ON verification_tokens(user_id);
CREATE INDEX idx_verification_type ON verification_tokens(type);
CREATE INDEX idx_verification_expires_at ON verification_tokens(expires_at);

-- Add comments
COMMENT ON COLUMN verification_tokens.type IS 'Token types: EMAIL_VERIFICATION, PASSWORD_RESET, SMS_OTP';
