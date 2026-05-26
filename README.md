<div align="center">

# Feature Flag & Release Management Service

**Manage feature releases across environments at runtime — no redeployments required.**

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
</div>

---

## Overview

This service decouples **feature deployment** from **feature activation**. Once deployed, flags can be toggled per environment via REST API — no code changes, no restarts, no risk to unrelated functionality.

**Core capabilities:**

| Capability | Description |
|---|---|
| Environment-scoped flags | Separate flag state for `DEV`, `QA`, and `PRODUCTION` |
| Runtime toggling | Enable or disable features instantly via a single API call |
| Immutable audit trail | Every toggle is recorded with before/after state and a timestamp |
| Input validation | Guards against unknown environments, duplicate flags, and missing resources |
| Interactive API docs | Swagger UI available out of the box |

---

## Table of Contents

- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Database Setup](#database-setup)
    - [Configuration](#configuration)
    - [Running the Service](#running-the-service)
    - [Running the Frontend](#running-the-frontend)
- [API Reference](#api-reference)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Roadmap](#roadmap)

---

## Architecture

```
src/main/java/com/teja/featureflagservice/
├── config/
│   └── OpenApiConfig.java             # Swagger / OpenAPI 3 configuration
├── controller/
│   └── FeatureController.java         # REST endpoints
├── dto/
│   ├── ErrorResponse.java
│   ├── FeatureRequest.java
│   ├── FeatureResponse.java
│   ├── ReleaseHistoryResponse.java
│   └── ToggleRequest.java
├── entity/
│   ├── FeatureFlag.java               # Flag state per environment
│   └── ReleaseHistory.java            # Immutable audit record
├── exception/
│   ├── DuplicateFeatureException.java
│   ├── FeatureNotFoundException.java
│   └── GlobalExceptionHandler.java    # Consistent error responses
├── repository/
│   ├── FeatureRepository.java
│   └── ReleaseHistoryRepository.java
├── service/
│   └── FeatureService.java            # Business logic and audit recording
└── FeatureflagserviceApplication.java
```

The project follows a strict layered pattern: **Controller → Service → Repository → Entity**. DTOs handle all input/output boundaries. A centralized `GlobalExceptionHandler` ensures every error returns a consistent response structure regardless of where it originates.

---

## Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 17 | LTS, records, sealed classes |
| Framework | Spring Boot | Auto-configuration, embedded server |
| Web | Spring Web MVC | REST API layer |
| Persistence | Spring Data JPA | ORM and repository abstraction |
| Database | MySQL 8 | Production datasource |
| Build | Maven | Dependency management and lifecycle |
| Utilities | Lombok | Boilerplate reduction |
| Validation | Bean Validation (Jakarta) | Request input validation |
| API Docs | Swagger / OpenAPI 3 | Interactive documentation |
| Testing | JUnit 5, MockMvc, H2 | Unit and integration tests |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.x (for local development)

### Database Setup

**Option 1 — Run the provided script:**

```bash
mysql -u root -p < database/mysql-setup.sql
```

**Option 2 — Run manually:**

```sql
CREATE DATABASE IF NOT EXISTS feature_flag_db;

-- Optional: dedicated service user
CREATE USER IF NOT EXISTS 'feature_flag_user'@'localhost'
  IDENTIFIED BY 'feature_flag_password';

GRANT ALL PRIVILEGES ON feature_flag_db.*
  TO 'feature_flag_user'@'localhost';

FLUSH PRIVILEGES;
```

### Configuration

Update `src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/feature_flag_db\
  ?createDatabaseIfNotExist=true\
  &useSSL=false\
  &allowPublicKeyRetrieval=true\
  &serverTimezone=UTC

spring.datasource.username=root
spring.datasource.password=root
```

> **Security note:** Do not commit credentials to version control. Use environment variables or a gitignored `application-local.properties` override in production.

### Running the Service

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The service starts on port `8080` by default.

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

### Running the Frontend

The React frontend is located in `frontend/` and proxies API calls to the Spring Boot backend on `http://localhost:8080`.

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on port `5173` by default.

| Resource | URL |
|---|---|
| React App | http://localhost:5173 |

Build the frontend:

```bash
npm run build
```

---

## API Reference

### Environments

All endpoints scope their data to one of three environments:

```
DEV  |  QA  |  PRODUCTION
```

---

### `POST /api/feature` — Create a Feature Flag

Registers a new feature flag. Returns `409 Conflict` if the flag already exists in that environment.

**Request:**
```json
{
  "featureName": "dark-mode",
  "environment": "DEV",
  "enabled": false
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "featureName": "dark-mode",
  "environment": "DEV",
  "enabled": false,
  "createdAt": "2026-05-26T05:45:00"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/feature \
  -H "Content-Type: application/json" \
  -d '{"featureName": "dark-mode", "environment": "DEV", "enabled": false}'
```

---

### `GET /api/features/{environment}` — List Flags by Environment

Returns all feature flags registered for the given environment.

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "featureName": "dark-mode",
    "environment": "DEV",
    "enabled": false,
    "createdAt": "2026-05-26T05:45:00"
  }
]
```

**cURL:**
```bash
curl http://localhost:8080/api/features/DEV
```

---

### `PUT /api/toggle-feature` — Toggle a Feature Flag

Enables or disables a feature flag. Automatically writes an entry to the release history audit log. Returns `404 Not Found` if the flag does not exist in the given environment.

**Request:**
```json
{
  "featureName": "dark-mode",
  "environment": "DEV",
  "enabled": true
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "featureName": "dark-mode",
  "environment": "DEV",
  "enabled": true,
  "createdAt": "2026-05-26T05:45:00"
}
```

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/toggle-feature \
  -H "Content-Type: application/json" \
  -d '{"featureName": "dark-mode", "environment": "DEV", "enabled": true}'
```

---

### `GET /api/release-history` — Audit Log

Returns the complete audit log of all toggle events, ordered by timestamp.

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "featureName": "dark-mode",
    "oldStatus": false,
    "newStatus": true,
    "environment": "DEV",
    "timestamp": "2026-05-26T05:50:00"
  }
]
```

**cURL:**
```bash
curl http://localhost:8080/api/release-history
```

---

## Error Handling

All errors return a consistent response envelope:

```json
{
  "timestamp": "2026-05-26T05:52:00",
  "status": 404,
  "error": "Not Found",
  "message": "Feature 'dark-mode' was not found in QA environment",
  "path": "/api/toggle-feature",
  "validationErrors": null
}
```

The `validationErrors` field is populated on `400 Bad Request` responses and contains field-level validation messages.

| Scenario | HTTP Status |
|---|---|
| Feature not found | `404 Not Found` |
| Feature already exists in environment | `409 Conflict` |
| Invalid environment value | `400 Bad Request` |
| Missing required field | `400 Bad Request` |

---

## Testing

The test suite runs against an **in-memory H2 database** using the `test` Spring profile — no MySQL installation is needed to run tests.

```bash
# macOS / Linux
./mvnw test

# Windows
mvnw.cmd test
```

**Coverage includes:**

- Feature flag creation and duplicate detection
- Environment-scoped flag listing
- Toggle workflow (enabled → disabled → enabled)
- Release history audit record creation and retrieval
- `404` handling for missing features

**Postman collection** for manual end-to-end testing:

```
postman/Enterprise-Feature-Flag-Service.postman_collection.json
```

---

## Roadmap

| Feature | Description |
|---|---|
| Role-based access control | Separate roles for release managers and auditors |
| Rollback endpoint | Restore a previous flag state from the release history |
| Approval workflows | Require sign-off before promoting a flag to `PRODUCTION` |
| Pagination and filtering | Cursor-based pagination for release history queries |
| Database migrations | Flyway or Liquibase for schema version control |
| Docker Compose | One-command local setup for MySQL and the service |
| CI integration | Publish OpenAPI contracts as part of the build pipeline |
