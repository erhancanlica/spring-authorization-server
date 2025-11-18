# Spring OAuth2 Authorization Server

A modern, production-ready OAuth2 Authorization Server built with Spring Boot 3.3.x, Spring Security 6.3.x, and Spring Authorization Server 1.3.x.

## Features

### 🔐 OAuth2 & OpenID Connect
- Full OAuth2 2.1 and OpenID Connect 1.0 support
- Authorization Code Flow with PKCE
- Client Credentials Flow
- Refresh Token Flow
- JWT token support
- Token introspection and revocation
- Well-known OpenID configuration endpoint

### 👤 Authentication Methods
- **Email & Password**: Traditional email/password authentication
- **Phone Number & Password**: Phone number-based authentication
- **Google OAuth2**: Social login integration
- **Two-Factor Authentication (2FA)**: TOTP-based 2FA with QR code generation

### ✉️ Email Verification
- Post-registration email verification
- Verification token generation and validation
- Token expiration control (24 hours)
- Resend verification email functionality

### 📱 SMS/Phone Verification
- Phone number verification with OTP
- SMS delivery via Twilio
- OTP expiration (10 minutes)
- Rate limiting for spam prevention

### 🔑 Password Management
- Forgot password flow
- Secure password reset via email
- Password strength validation
- BCrypt password hashing

### 🚦 Rate Limiting
- Login attempt rate limiting
- SMS OTP rate limiting
- API request rate limiting
- IP-based and user-based throttling
- Configurable limits and time windows

### 🔒 Security Features
- BCrypt password hashing (strength 10)
- CSRF protection
- CORS configuration
- Secure HTTP headers
- SQL injection prevention (JPA/Hibernate)
- XSS protection
- Account locking after failed attempts
- JWT token-based authentication

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.3.5
- **Spring Security**: 6.3.4
- **Spring Authorization Server**: 1.3.2
- **Database**: PostgreSQL
- **Migrations**: Flyway
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 16+
- Docker & Docker Compose (optional)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/erhancanlica/spring-authorization-server.git
   cd spring-authorization-server
   ```

2. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start PostgreSQL** (if not using Docker)
   ```bash
   # Using Docker
   docker run -d \
     --name authserver-postgres \
     -e POSTGRES_DB=authserver \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:16-alpine
   ```

4. **Build the application**
   ```bash
   mvn clean package -DskipTests
   ```

5. **Run the application**
   ```bash
   java -jar target/spring-authorization-server-1.0.0.jar
   ```

   Or with Maven:
   ```bash
   mvn spring-boot:run
   ```

### Using Docker Compose

The easiest way to run the entire stack:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- Spring Authorization Server on port 8080

## Configuration

### Environment Variables

Key configuration options (see `.env.example` for full list):

```env
# Database
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_HOST=localhost
DB_PORT=5432
DB_NAME=authserver

# JWT
JWT_SECRET=your_jwt_secret_minimum_256_bits
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

# Email (Gmail example)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Twilio SMS
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
TWILIO_FROM_NUMBER=+1234567890
```

### Application Configuration

Key settings in `application.yml`:

```yaml
# Rate Limiting
rate-limit:
  login-attempts: 5        # Max login attempts
  window-minutes: 15        # Time window in minutes
  sms-attempts: 3          # Max SMS attempts
  window-minutes-sms: 60   # SMS time window

# JWT
jwt:
  expiration: 3600000           # 1 hour
  refresh-expiration: 86400000  # 24 hours
```

## API Documentation

### Authentication Endpoints

#### Register with Email
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!"
}
```

#### Register with Phone
```http
POST /api/auth/register/phone
Content-Type: application/json

{
  "phoneNumber": "+1234567890",
  "password": "Password123!"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "identifier": "user@example.com",
  "password": "Password123!",
  "twoFactorCode": "123456"  // Optional, required if 2FA enabled
}
```

Response:
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

#### Refresh Token
```http
POST /api/auth/refresh?refreshToken=<refresh_token>
```

### Email Verification

#### Verify Email
```http
POST /api/auth/verify-email
Content-Type: application/json

{
  "token": "verification_token_from_email"
}
```

#### Resend Verification Email
```http
POST /api/auth/resend-verification?email=user@example.com
```

### Phone Verification

#### Send OTP
```http
POST /api/auth/send-otp
Content-Type: application/json

{
  "phoneNumber": "+1234567890"
}
```

#### Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+1234567890",
  "otpCode": "123456"
}
```

### Password Reset

#### Request Password Reset
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "reset_token_from_email",
  "newPassword": "NewPassword123!"
}
```

### Two-Factor Authentication

All 2FA endpoints require authentication (Bearer token).

#### Enable 2FA (Setup)
```http
POST /api/auth/2fa/enable
Authorization: Bearer <access_token>
```

Response includes QR code for authenticator app setup.

#### Verify and Enable 2FA
```http
POST /api/auth/2fa/verify?code=123456
Authorization: Bearer <access_token>
```

#### Disable 2FA
```http
POST /api/auth/2fa/disable?code=123456
Authorization: Bearer <access_token>
```

#### Get QR Code
```http
GET /api/auth/2fa/qrcode
Authorization: Bearer <access_token>
```

### OAuth2 Endpoints

#### Authorization Endpoint
```
GET /oauth2/authorize?response_type=code&client_id=default-client&redirect_uri=http://localhost:8080/authorized&scope=openid profile email
```

#### Token Endpoint
```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic <base64(client_id:client_secret)>

grant_type=authorization_code
&code=<authorization_code>
&redirect_uri=http://localhost:8080/authorized
```

#### Token Introspection
```http
POST /oauth2/introspect
Authorization: Basic <base64(client_id:client_secret)>
Content-Type: application/x-www-form-urlencoded

token=<access_token>
```

#### Token Revocation
```http
POST /oauth2/revoke
Authorization: Basic <base64(client_id:client_secret)>
Content-Type: application/x-www-form-urlencoded

token=<access_token_or_refresh_token>
```

#### OpenID Configuration
```
GET /.well-known/openid-configuration
```

### Health Check
```http
GET /actuator/health
```

## Database Schema

### Users Table
- `id`: UUID (Primary Key)
- `email`: VARCHAR(255) (Unique, Indexed)
- `phone_number`: VARCHAR(20) (Unique, Indexed)
- `password_hash`: VARCHAR(255)
- `email_verified`: BOOLEAN
- `phone_verified`: BOOLEAN
- `two_factor_enabled`: BOOLEAN
- `two_factor_secret`: VARCHAR(255)
- `google_id`: VARCHAR(255) (Unique)
- `account_locked`: BOOLEAN
- `failed_login_attempts`: INTEGER
- `last_login`: TIMESTAMP
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP

### Verification Tokens Table
- `id`: UUID (Primary Key)
- `user_id`: UUID (Foreign Key)
- `token`: VARCHAR(255) (Unique)
- `type`: VARCHAR(50) (EMAIL_VERIFICATION, PASSWORD_RESET, SMS_OTP)
- `expires_at`: TIMESTAMP
- `used`: BOOLEAN
- `created_at`: TIMESTAMP

### OAuth2 Clients Table
- `id`: UUID (Primary Key)
- `client_id`: VARCHAR(255) (Unique)
- `client_secret`: VARCHAR(255)
- `client_name`: VARCHAR(255)
- `redirect_uris`: TEXT
- `scopes`: TEXT
- `authorized_grant_types`: TEXT
- `access_token_validity`: INTEGER
- `refresh_token_validity`: INTEGER
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP

### Rate Limit Tracking Table
- `id`: UUID (Primary Key)
- `identifier`: VARCHAR(255)
- `action_type`: VARCHAR(50)
- `attempt_count`: INTEGER
- `window_start`: TIMESTAMP
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP

## Password Policy

- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character (@$!%*?&)

## Default OAuth2 Clients

Two clients are pre-configured for testing:

### Default Client
- **Client ID**: `default-client`
- **Client Secret**: `secret`
- **Grant Types**: authorization_code, refresh_token, client_credentials
- **Redirect URIs**: http://localhost:8080/authorized, http://localhost:3000/callback
- **Scopes**: openid, profile, email, read, write

### Public Client (Mobile/SPA)
- **Client ID**: `public-client`
- **Client Secret**: `secret`
- **Grant Types**: authorization_code, refresh_token
- **Redirect URIs**: http://localhost:3000/callback, myapp://callback
- **Scopes**: openid, profile, email, read
- **PKCE**: Required

## Development

### Building from Source

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Package
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

### Database Migration

Flyway migrations are automatically applied on startup. Migration scripts are located in:
```
src/main/resources/db/migration/
```

To manually run migrations:
```bash
mvn flyway:migrate
```

### Running Tests

```bash
mvn test
```

## Production Deployment

### Security Checklist

- [ ] Change all default passwords and secrets
- [ ] Use strong JWT secret (minimum 256 bits)
- [ ] Configure proper CORS origins
- [ ] Enable HTTPS/TLS
- [ ] Set up proper database credentials
- [ ] Configure production mail server
- [ ] Set up proper logging and monitoring
- [ ] Enable rate limiting with appropriate limits
- [ ] Review and adjust token expiration times
- [ ] Set up database backups
- [ ] Configure firewall rules
- [ ] Review OAuth2 client configurations
- [ ] Set up proper environment variable management

### Docker Production Deployment

1. Build production image:
   ```bash
   docker build -t spring-authserver:latest .
   ```

2. Run with production configuration:
   ```bash
   docker run -d \
     --name authserver \
     -p 8080:8080 \
     -e DB_HOST=your-postgres-host \
     -e DB_USERNAME=your-username \
     -e DB_PASSWORD=your-password \
     -e JWT_SECRET=your-production-jwt-secret \
     spring-authserver:latest
   ```

## Monitoring

### Health Check Endpoints

- **Health**: `GET /actuator/health`
- **Info**: `GET /actuator/info`

### Logging

The application uses SLF4J with Logback. Logs are output to console by default.

Configure logging levels in `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.authserver: DEBUG
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify PostgreSQL is running
   - Check database credentials in environment variables
   - Ensure database exists

2. **Email Sending Failed**
   - Verify SMTP credentials
   - For Gmail, use App Password (not regular password)
   - Check firewall rules for SMTP port

3. **SMS Not Sending**
   - Verify Twilio credentials
   - Check Twilio account balance
   - Ensure phone number format is correct (+1234567890)

4. **Token Validation Failed**
   - Ensure JWT secret is consistent across restarts
   - Check token expiration times
   - Verify clock synchronization

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create an issue on GitHub
- Email: support@example.com

## Acknowledgments

- Spring Framework team for excellent OAuth2 support
- All contributors to this project
