-- Create OAuth2 clients table
CREATE TABLE oauth2_clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id VARCHAR(255) NOT NULL UNIQUE,
    client_secret VARCHAR(255) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    redirect_uris TEXT NOT NULL,
    scopes TEXT NOT NULL,
    authorized_grant_types TEXT NOT NULL,
    access_token_validity INTEGER DEFAULT 3600,
    refresh_token_validity INTEGER DEFAULT 86400,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index
CREATE INDEX idx_oauth2_client_id ON oauth2_clients(client_id);

-- Add comments
COMMENT ON COLUMN oauth2_clients.redirect_uris IS 'Comma-separated list of redirect URIs';
COMMENT ON COLUMN oauth2_clients.scopes IS 'Comma-separated list of scopes (e.g., openid, profile, email)';
COMMENT ON COLUMN oauth2_clients.authorized_grant_types IS 'Comma-separated list of grant types (e.g., authorization_code, refresh_token, client_credentials)';
COMMENT ON COLUMN oauth2_clients.access_token_validity IS 'Access token validity in seconds';
COMMENT ON COLUMN oauth2_clients.refresh_token_validity IS 'Refresh token validity in seconds';
