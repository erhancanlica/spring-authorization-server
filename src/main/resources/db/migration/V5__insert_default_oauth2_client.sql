-- Insert a default OAuth2 client for testing and development
-- Client Secret: secret (BCrypt encoded)
-- Note: In production, use a secure secret and proper encoding

INSERT INTO oauth2_clients (
    id,
    client_id,
    client_secret,
    client_name,
    redirect_uris,
    scopes,
    authorized_grant_types,
    access_token_validity,
    refresh_token_validity
) VALUES (
    gen_random_uuid(),
    'default-client',
    '$2a$10$EOs8VROb14e7ZnydvXECA.4LoIhPOoFHKvVF/iBZ/ker17Eocz4Vi',
    'Default Client',
    'http://localhost:8080/authorized,http://localhost:3000/callback',
    'openid,profile,email,read,write',
    'authorization_code,refresh_token,client_credentials',
    3600,
    86400
);

-- Add a public client for mobile/SPA applications
INSERT INTO oauth2_clients (
    id,
    client_id,
    client_secret,
    client_name,
    redirect_uris,
    scopes,
    authorized_grant_types,
    access_token_validity,
    refresh_token_validity
) VALUES (
    gen_random_uuid(),
    'public-client',
    '$2a$10$EOs8VROb14e7ZnydvXECA.4LoIhPOoFHKvVF/iBZ/ker17Eocz4Vi',
    'Public Client (Mobile/SPA)',
    'http://localhost:3000/callback,myapp://callback',
    'openid,profile,email,read',
    'authorization_code,refresh_token',
    1800,
    604800
);
