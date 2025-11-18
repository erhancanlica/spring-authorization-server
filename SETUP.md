# Development Environment Setup

This guide will help you set up your development environment for the Spring OAuth2 Authorization Server.

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17 or higher**
   - Download from [Eclipse Temurin](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
   - Verify installation:
     ```bash
     java -version
     javac -version
     ```

2. **Apache Maven 3.6+**
   - Download from [Maven Downloads](https://maven.apache.org/download.cgi)
   - Verify installation:
     ```bash
     mvn -version
     ```

3. **PostgreSQL 16+**
   - Download from [PostgreSQL Downloads](https://www.postgresql.org/download/)
   - Or use Docker (recommended for development):
     ```bash
     docker run -d --name authserver-postgres \
       -e POSTGRES_DB=authserver \
       -e POSTGRES_USER=postgres \
       -e POSTGRES_PASSWORD=postgres \
       -p 5432:5432 \
       postgres:16-alpine
     ```

4. **Git**
   - Download from [Git Downloads](https://git-scm.com/downloads)

### Optional Software

1. **Docker & Docker Compose**
   - Download from [Docker Desktop](https://www.docker.com/products/docker-desktop)
   - Useful for running PostgreSQL and the entire application stack

2. **IDE**
   - [IntelliJ IDEA](https://www.jetbrains.com/idea/) (recommended)
   - [Eclipse](https://www.eclipse.org/)
   - [VS Code](https://code.visualstudio.com/) with Java extensions

3. **Database Client**
   - [DBeaver](https://dbeaver.io/)
   - [pgAdmin](https://www.pgadmin.org/)
   - [DataGrip](https://www.jetbrains.com/datagrip/)

4. **API Testing Tools**
   - [Postman](https://www.postman.com/)
   - [Insomnia](https://insomnia.rest/)
   - [cURL](https://curl.se/)

## Project Setup

### 1. Clone the Repository

```bash
git clone https://github.com/erhancanlica/spring-authorization-server.git
cd spring-authorization-server
```

### 2. Set Up Database

#### Option A: Using Docker (Recommended)

```bash
docker run -d --name authserver-postgres \
  -e POSTGRES_DB=authserver \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

#### Option B: Local PostgreSQL Installation

1. Install PostgreSQL
2. Create database:
   ```sql
   CREATE DATABASE authserver;
   ```

### 3. Configure Environment Variables

Create a `.env` file in the project root:

```bash
cp .env.example .env
```

Edit `.env` with your configuration:

```env
# Database Configuration
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_HOST=localhost
DB_PORT=5432
DB_NAME=authserver

# JWT Configuration (Generate a strong secret for production!)
JWT_SECRET=your_jwt_secret_key_minimum_256_bits_long_change_this_in_production
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

# Email Configuration (Gmail example)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Google OAuth2 (Optional - get from Google Cloud Console)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Twilio SMS (Optional - get from Twilio Console)
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
TWILIO_FROM_NUMBER=+1234567890

# Two-Factor Authentication
TWO_FACTOR_ISSUER=SpringAuthServer

# Server Configuration
SERVER_PORT=8080
```

### 4. Set Up Email (Gmail Example)

For Gmail, you need to use an App Password:

1. Go to [Google Account Settings](https://myaccount.google.com/)
2. Navigate to Security → 2-Step Verification
3. Scroll down to App passwords
4. Generate a new app password
5. Use this password in `MAIL_PASSWORD` environment variable

### 5. Set Up Google OAuth2 (Optional)

If you want to enable Google login:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Go to Credentials → Create Credentials → OAuth 2.0 Client ID
5. Set authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
6. Copy Client ID and Client Secret to environment variables

### 6. Set Up Twilio SMS (Optional)

If you want to enable SMS OTP:

1. Sign up at [Twilio](https://www.twilio.com/)
2. Get Account SID and Auth Token from dashboard
3. Get a phone number from Twilio
4. Copy credentials to environment variables

## Building the Project

### Compile

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Package

```bash
mvn clean package
```

Skip tests if needed:
```bash
mvn clean package -DskipTests
```

## Running the Application

### Option 1: Using Maven

```bash
mvn spring-boot:run
```

### Option 2: Using JAR

```bash
java -jar target/spring-authorization-server-1.0.0.jar
```

### Option 3: Using Docker Compose (Recommended)

This starts both PostgreSQL and the application:

```bash
docker-compose up -d
```

View logs:
```bash
docker-compose logs -f app
```

Stop services:
```bash
docker-compose down
```

### Option 4: From IDE

In IntelliJ IDEA:
1. Open the project
2. Wait for Maven to import dependencies
3. Find `AuthServerApplication.java`
4. Right-click → Run 'AuthServerApplication'

## Verifying the Setup

### 1. Check Application Status

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### 2. Check Database Connection

The application should start without errors. Check logs for:
```
Flyway migration completed successfully
```

### 3. Check OAuth2 Endpoints

```bash
curl http://localhost:8080/.well-known/openid-configuration
```

Should return OpenID Connect configuration.

### 4. Test Registration

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

## IDE Setup

### IntelliJ IDEA

1. **Import Project**
   - File → Open → Select project directory
   - Maven should auto-import

2. **Configure Lombok**
   - File → Settings → Plugins → Install "Lombok"
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Enable "Enable annotation processing"

3. **Configure Run Configuration**
   - Run → Edit Configurations → Add New → Spring Boot
   - Main class: `com.authserver.AuthServerApplication`
   - Environment variables: Load from `.env` or set manually

4. **Database Tool**
   - View → Tool Windows → Database
   - Add PostgreSQL data source
   - Test connection

### VS Code

1. **Install Extensions**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Open Project**
   - File → Open Folder → Select project directory

3. **Configure Run**
   - Create `.vscode/launch.json`:
     ```json
     {
       "version": "0.2.0",
       "configurations": [
         {
           "type": "java",
           "name": "Spring Boot App",
           "request": "launch",
           "mainClass": "com.authserver.AuthServerApplication",
           "projectName": "spring-authorization-server"
         }
       ]
     }
     ```

### Eclipse

1. **Import Project**
   - File → Import → Maven → Existing Maven Projects
   - Select project directory

2. **Install Lombok**
   - Download lombok.jar
   - Run: `java -jar lombok.jar`
   - Select Eclipse installation directory

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

Edit code, add tests, update documentation.

### 3. Run Tests

```bash
mvn test
```

### 4. Build

```bash
mvn clean package
```

### 5. Commit Changes

```bash
git add .
git commit -m "Add your feature"
git push origin feature/your-feature-name
```

### 6. Create Pull Request

On GitHub, create a pull request from your feature branch.

## Database Management

### View Database Schema

```bash
docker exec -it authserver-postgres psql -U postgres -d authserver -c "\dt"
```

### Query Users Table

```bash
docker exec -it authserver-postgres psql -U postgres -d authserver -c "SELECT * FROM users;"
```

### Reset Database

```bash
docker-compose down -v
docker-compose up -d
```

### Manual Migration

```bash
mvn flyway:migrate
```

### Rollback Migration

```bash
mvn flyway:clean
mvn flyway:migrate
```

## Debugging

### Enable Debug Logging

In `application.yml`:
```yaml
logging:
  level:
    com.authserver: DEBUG
    org.springframework.security: DEBUG
```

### Remote Debugging

Run with debug enabled:
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar target/spring-authorization-server-1.0.0.jar
```

Connect debugger to port 5005.

### IDE Debugging

In IntelliJ IDEA:
1. Set breakpoints in code
2. Run → Debug 'AuthServerApplication'

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### Test Coverage

```bash
mvn clean test jacoco:report
```

View report: `target/site/jacoco/index.html`

## Common Issues

### Port Already in Use

Kill process using port 8080:
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <pid> /F
```

### Database Connection Error

1. Check PostgreSQL is running:
   ```bash
   docker ps | grep postgres
   ```

2. Check connection parameters in `.env`

3. Try connecting manually:
   ```bash
   psql -h localhost -U postgres -d authserver
   ```

### Maven Dependency Issues

Clear Maven cache:
```bash
mvn dependency:purge-local-repository
mvn clean install
```

### Lombok Not Working

1. Install Lombok plugin in IDE
2. Enable annotation processing
3. Restart IDE

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Spring Authorization Server Documentation](https://spring.io/projects/spring-authorization-server)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Maven Documentation](https://maven.apache.org/guides/)

## Getting Help

If you encounter issues:
1. Check this guide
2. Review application logs
3. Search existing GitHub issues
4. Create a new issue with details
5. Contact support

## Next Steps

After setting up your development environment:
1. Review the [README.md](README.md) for project overview
2. Check [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for API details
3. Explore the codebase
4. Run the example requests in Postman
5. Start developing your features!
