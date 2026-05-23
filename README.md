# Tinto Backend

REST API backend for the Tinto loyalty program. Built with Quarkus (Kotlin, reactive stack) and PostgreSQL 18.

## Tech Stack

- **Runtime**: Quarkus 3.30.2, Kotlin 2.3.21, Java 25
- **HTTP**: `quarkus-rest` (reactive, Vert.x-based)
- **Database**: PostgreSQL 18, `quarkus-reactive-pg-client`
- **Migrations**: Flyway
- **Auth**: Google & Apple Sign-In (ID token exchange → own JWT via SmallRye JWT)

## Prerequisites

- Java 25+
- Docker (for local Postgres and tests)

## Setup

### 1. Configure application properties

Edit `src/main/resources/application.properties` and fill in:

```properties
tinto.auth.google.client-id=<your-google-client-id>
tinto.auth.apple.client-id=<your-apple-bundle-id>
```

### 2. Generate JWT key pair

```bash
openssl genrsa -out src/main/resources/privateKey.pem 2048
openssl rsa -in src/main/resources/privateKey.pem -pubout -out src/main/resources/publicKey.pem
```

Both files are git-ignored and must be provisioned on every environment.

### 3. Run in dev mode

```bash
./gradlew quarkusDev
```

Quarkus Dev Services automatically starts a `postgres:18-alpine` container on the first run — no manual Docker setup needed. Flyway applies migrations on startup.

## API

### Authentication

**`POST /auth/login`**

Exchange a Google or Apple ID token for a Tinto JWT.

```json
// Request
{ "provider": "GOOGLE", "idToken": "eyJ..." }

// Response
{ "token": "eyJ...", "userId": "uuid", "email": "user@example.com" }
```

> **Apple note:** Apple only sends `name` on the first login. Pass it as an extra field on registration:
> `{ "provider": "APPLE", "idToken": "eyJ...", "name": "Alice" }`

Use the returned `token` as a Bearer token on all subsequent requests.

### Vendors

All vendor endpoints require `Authorization: Bearer <token>`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/vendors?q=cafes&name=<query>` | Search vendors by type and name |
| `GET` | `/vendors/cafes` | List all cafes |

## Generating a dev JWT token

To test authenticated endpoints locally without going through Google/Apple:

```bash
./gradlew generateToken
```

Copy the printed token and use it as `Authorization: Bearer <token>` in your HTTP client.

## Running Tests

Tests use Testcontainers and spin up a real PostgreSQL 18 instance automatically. Docker must be running.

```bash
./gradlew test
```

## Database

Migrations live in `src/main/resources/db/migration/`. Flyway applies them at startup (`migrate-at-start=true`).

| Table | Description |
|-------|-------------|
| `users` | Registered users, keyed by OAuth provider + subject ID. Primary key is UUID v7 (time-ordered). |
