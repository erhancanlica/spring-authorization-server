# API Documentation

Complete API reference for the Spring OAuth2 Authorization Server.

## Base URL

```
http://localhost:8080
```

## Authentication

Most endpoints require authentication via Bearer token in the Authorization header:

```
Authorization: Bearer <access_token>
```

## Response Format

All API responses follow this standard format:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### Validation Error Response
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "fieldName": "Error message for this field",
    "anotherField": "Another error message"
  }
}
```

## HTTP Status Codes

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required or invalid
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error

---

## Authentication Endpoints

### Register with Email

Create a new user account with email.

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**Validation Rules:**
- `email`: Valid email format, required
- `password`: Minimum 8 characters, must contain uppercase, lowercase, digit, and special character

**Success Response (200):**
```json
{
  "success": true,
  "message": "Registration successful. Please check your email to verify your account.",
  "data": null
}
```

**Error Responses:**
- `400`: Email already registered
- `400`: Validation failed

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123!"
  }'
```

---

### Register with Phone Number

Create a new user account with phone number.

**Endpoint:** `POST /api/auth/register/phone`

**Request Body:**
```json
{
  "phoneNumber": "+1234567890",
  "password": "Password123!"
}
```

**Validation Rules:**
- `phoneNumber`: E.164 format (e.g., +1234567890), required
- `password`: Minimum 8 characters, must contain uppercase, lowercase, digit, and special character

**Success Response (200):**
```json
{
  "success": true,
  "message": "Registration successful. Please verify your phone number.",
  "data": null
}
```

**Error Responses:**
- `400`: Phone number already registered
- `400`: Validation failed

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/register/phone \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "password": "Password123!"
  }'
```

---

### Login

Authenticate user and receive access tokens.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "identifier": "user@example.com",
  "password": "Password123!",
  "twoFactorCode": "123456"
}
```

**Fields:**
- `identifier`: Email or phone number (required)
- `password`: User password (required)
- `twoFactorCode`: 6-digit TOTP code (optional, required if 2FA enabled)

**Success Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "scope": "openid profile email"
  }
}
```

**Error Responses:**
- `401`: Invalid credentials
- `401`: Account locked
- `401`: Email/phone not verified
- `401`: Invalid 2FA code
- `429`: Too many login attempts

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "Password123!"
  }'
```

---

### Refresh Token

Get new access token using refresh token.

**Endpoint:** `POST /api/auth/refresh`

**Query Parameters:**
- `refreshToken`: The refresh token received during login

**Success Response (200):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "scope": "openid profile email"
  }
}
```

**Error Responses:**
- `401`: Invalid or expired refresh token

**Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/refresh?refreshToken=<your_refresh_token>"
```

---

## Email Verification Endpoints

### Verify Email

Verify user's email address with token from email.

**Endpoint:** `POST /api/auth/verify-email`

**Request Body:**
```json
{
  "token": "verification_token_from_email"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": null
}
```

**Alternative:** `GET /api/auth/verify-email?token=<token>`

**Error Responses:**
- `400`: Invalid or expired token
- `400`: Token already used

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "token": "verification_token_from_email"
  }'
```

---

### Resend Verification Email

Request a new verification email.

**Endpoint:** `POST /api/auth/resend-verification`

**Query Parameters:**
- `email`: User's email address

**Success Response (200):**
```json
{
  "success": true,
  "message": "Verification email sent",
  "data": null
}
```

**Error Responses:**
- `400`: User not found
- `400`: Email already verified

**Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/resend-verification?email=user@example.com"
```

---

## Phone Verification Endpoints

### Send OTP

Send OTP code to phone number via SMS.

**Endpoint:** `POST /api/auth/send-otp`

**Request Body:**
```json
{
  "phoneNumber": "+1234567890"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "data": null
}
```

**Error Responses:**
- `400`: User not found
- `400`: Phone already verified
- `429`: Too many SMS attempts

**Rate Limit:** 3 attempts per hour

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890"
  }'
```

---

### Verify OTP

Verify phone number with OTP code.

**Endpoint:** `POST /api/auth/verify-otp`

**Request Body:**
```json
{
  "phoneNumber": "+1234567890",
  "otpCode": "123456"
}
```

**Validation Rules:**
- `otpCode`: 6 digits, required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Phone number verified successfully",
  "data": null
}
```

**Error Responses:**
- `400`: Invalid OTP
- `400`: OTP expired (10 minutes)
- `400`: OTP already used

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "otpCode": "123456"
  }'
```

---

## Password Reset Endpoints

### Forgot Password

Request password reset link via email.

**Endpoint:** `POST /api/auth/forgot-password`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Password reset link sent to your email",
  "data": null
}
```

**Error Responses:**
- `400`: User not found

**Note:** Token expires in 1 hour

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

---

### Reset Password

Reset password using token from email.

**Endpoint:** `POST /api/auth/reset-password`

**Request Body:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "NewPassword123!"
}
```

**Validation Rules:**
- `newPassword`: Minimum 8 characters, must contain uppercase, lowercase, digit, and special character

**Success Response (200):**
```json
{
  "success": true,
  "message": "Password reset successful",
  "data": null
}
```

**Alternative:** `GET /api/auth/reset-password?token=<token>&newPassword=<password>`

**Error Responses:**
- `400`: Invalid or expired token
- `400`: Token already used
- `400`: Validation failed

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "reset_token_from_email",
    "newPassword": "NewPassword123!"
  }'
```

---

## Two-Factor Authentication Endpoints

All 2FA endpoints require authentication.

### Enable 2FA (Setup)

Initiate 2FA setup and receive QR code.

**Endpoint:** `POST /api/auth/2fa/enable`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Two-factor authentication setup initiated",
  "data": {
    "secret": "JBSWY3DPEHPK3PXP",
    "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANS...",
    "manualEntryKey": "JBSWY3DPEHPK3PXP"
  }
}
```

**Instructions:**
1. Scan QR code with authenticator app (Google Authenticator, Authy, etc.)
2. Or manually enter the secret key
3. Verify with code in next step

**Error Responses:**
- `400`: 2FA already enabled
- `401`: Unauthorized

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/2fa/enable \
  -H "Authorization: Bearer <access_token>"
```

---

### Verify and Enable 2FA

Verify TOTP code and enable 2FA.

**Endpoint:** `POST /api/auth/2fa/verify`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `code`: 6-digit TOTP code from authenticator app

**Success Response (200):**
```json
{
  "success": true,
  "message": "Two-factor authentication enabled successfully",
  "data": null
}
```

**Error Responses:**
- `400`: Invalid verification code
- `400`: 2FA not set up
- `401`: Unauthorized

**Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/2fa/verify?code=123456" \
  -H "Authorization: Bearer <access_token>"
```

---

### Disable 2FA

Disable two-factor authentication.

**Endpoint:** `POST /api/auth/2fa/disable`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `code`: 6-digit TOTP code from authenticator app

**Success Response (200):**
```json
{
  "success": true,
  "message": "Two-factor authentication disabled successfully",
  "data": null
}
```

**Error Responses:**
- `400`: Invalid verification code
- `400`: 2FA not enabled
- `401`: Unauthorized

**Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/2fa/disable?code=123456" \
  -H "Authorization: Bearer <access_token>"
```

---

### Get QR Code

Get QR code for 2FA setup.

**Endpoint:** `GET /api/auth/2fa/qrcode`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "QR code generated",
  "data": {
    "secret": "JBSWY3DPEHPK3PXP",
    "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANS...",
    "manualEntryKey": "JBSWY3DPEHPK3PXP"
  }
}
```

**Example:**
```bash
curl -X GET http://localhost:8080/api/auth/2fa/qrcode \
  -H "Authorization: Bearer <access_token>"
```

---

## OAuth2 Endpoints

### Authorization Endpoint

Start OAuth2 authorization code flow.

**Endpoint:** `GET /oauth2/authorize`

**Query Parameters:**
- `response_type`: `code` (required)
- `client_id`: OAuth2 client ID (required)
- `redirect_uri`: Callback URL (required)
- `scope`: Space-separated scopes (e.g., `openid profile email`)
- `state`: CSRF protection token (recommended)
- `code_challenge`: PKCE code challenge (for public clients)
- `code_challenge_method`: `S256` (for PKCE)

**Example:**
```
http://localhost:8080/oauth2/authorize?response_type=code&client_id=default-client&redirect_uri=http://localhost:8080/authorized&scope=openid%20profile%20email&state=xyz
```

**Flow:**
1. User is redirected to login if not authenticated
2. User consents to requested scopes
3. User is redirected back to `redirect_uri` with authorization code

**Callback:**
```
http://localhost:8080/authorized?code=<authorization_code>&state=xyz
```

---

### Token Endpoint

Exchange authorization code for tokens.

**Endpoint:** `POST /oauth2/token`

**Headers:**
```
Authorization: Basic <base64(client_id:client_secret)>
Content-Type: application/x-www-form-urlencoded
```

**Request Body (Authorization Code):**
```
grant_type=authorization_code
&code=<authorization_code>
&redirect_uri=http://localhost:8080/authorized
&code_verifier=<pkce_verifier>
```

**Request Body (Refresh Token):**
```
grant_type=refresh_token
&refresh_token=<refresh_token>
```

**Request Body (Client Credentials):**
```
grant_type=client_credentials
&scope=read write
```

**Success Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "openid profile email"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Authorization: Basic ZGVmYXVsdC1jbGllbnQ6c2VjcmV0" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=<code>&redirect_uri=http://localhost:8080/authorized"
```

---

### Token Introspection

Check if token is active and get token information.

**Endpoint:** `POST /oauth2/introspect`

**Headers:**
```
Authorization: Basic <base64(client_id:client_secret)>
Content-Type: application/x-www-form-urlencoded
```

**Request Body:**
```
token=<access_token_or_refresh_token>
```

**Success Response:**
```json
{
  "active": true,
  "sub": "user@example.com",
  "client_id": "default-client",
  "exp": 1699999999,
  "iat": 1699999999,
  "scope": "openid profile email"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/oauth2/introspect \
  -H "Authorization: Basic ZGVmYXVsdC1jbGllbnQ6c2VjcmV0" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=<access_token>"
```

---

### Token Revocation

Revoke access or refresh token.

**Endpoint:** `POST /oauth2/revoke`

**Headers:**
```
Authorization: Basic <base64(client_id:client_secret)>
Content-Type: application/x-www-form-urlencoded
```

**Request Body:**
```
token=<access_token_or_refresh_token>
&token_type_hint=access_token
```

**Success Response:** `200 OK` (empty body)

**Example:**
```bash
curl -X POST http://localhost:8080/oauth2/revoke \
  -H "Authorization: Basic ZGVmYXVsdC1jbGllbnQ6c2VjcmV0" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=<access_token>"
```

---

### OpenID Configuration

Get OpenID Connect discovery document.

**Endpoint:** `GET /.well-known/openid-configuration`

**Success Response:**
```json
{
  "issuer": "http://localhost:8080",
  "authorization_endpoint": "http://localhost:8080/oauth2/authorize",
  "token_endpoint": "http://localhost:8080/oauth2/token",
  "jwks_uri": "http://localhost:8080/oauth2/jwks",
  "response_types_supported": ["code"],
  "grant_types_supported": ["authorization_code", "refresh_token", "client_credentials"],
  "subject_types_supported": ["public"],
  "id_token_signing_alg_values_supported": ["RS256"],
  "scopes_supported": ["openid", "profile", "email"],
  "token_endpoint_auth_methods_supported": ["client_secret_basic", "client_secret_post"],
  "claims_supported": ["sub", "aud", "iss", "exp", "iat"]
}
```

**Example:**
```bash
curl http://localhost:8080/.well-known/openid-configuration
```

---

## Health & Monitoring Endpoints

### Health Check

Check application health status.

**Endpoint:** `GET /actuator/health`

**Success Response:**
```json
{
  "status": "UP"
}
```

**Example:**
```bash
curl http://localhost:8080/actuator/health
```

---

### Application Info

Get application information.

**Endpoint:** `GET /actuator/info`

**Success Response:**
```json
{
  "app": {
    "name": "Spring OAuth2 Authorization Server",
    "version": "1.0.0"
  }
}
```

**Example:**
```bash
curl http://localhost:8080/actuator/info
```

---

## Rate Limiting

The API implements rate limiting on sensitive endpoints:

### Login Endpoint
- **Limit**: 5 attempts per 15 minutes
- **Identifier**: Client IP address
- **Status**: 429 Too Many Requests

### SMS OTP Endpoint
- **Limit**: 3 attempts per 60 minutes
- **Identifier**: Client IP address
- **Status**: 429 Too Many Requests

### Rate Limit Error Response
```json
{
  "success": false,
  "message": "Too many attempts. Please try again after 15 minutes.",
  "data": null
}
```

---

## Error Codes

| Code | Message | Description |
|------|---------|-------------|
| `USER_NOT_FOUND` | User not found | No user exists with provided identifier |
| `INVALID_CREDENTIALS` | Invalid credentials | Wrong password |
| `ACCOUNT_LOCKED` | Account is locked | Too many failed login attempts |
| `EMAIL_NOT_VERIFIED` | Email not verified | User must verify email before login |
| `PHONE_NOT_VERIFIED` | Phone not verified | User must verify phone before login |
| `INVALID_TOKEN` | Invalid or expired token | Token is invalid, expired, or used |
| `INVALID_2FA_CODE` | Invalid 2FA code | Wrong TOTP code |
| `RATE_LIMIT_EXCEEDED` | Rate limit exceeded | Too many requests |
| `VALIDATION_ERROR` | Validation failed | Request data validation failed |

---

## Postman Collection

Import this collection into Postman for easy API testing:

[Download Postman Collection](postman_collection.json)

Or create a new collection with the examples above.

---

## Testing Tips

1. **Use variables** in Postman for `access_token` and `refresh_token`
2. **Save tokens** after login for subsequent requests
3. **Use environment variables** for base URL
4. **Test error cases** by providing invalid data
5. **Check rate limiting** by making multiple rapid requests
6. **Test token expiration** by waiting or adjusting expiration time

---

## Support

For API questions or issues:
- Create an issue on GitHub
- Check existing documentation
- Review application logs
