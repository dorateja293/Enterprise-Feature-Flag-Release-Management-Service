# Enterprise Feature Flag and Release Management Service

Enterprise Feature Flag and Release Management Service is a Java 17 Spring Boot backend for managing feature releases across DEV, QA, and PRODUCTION environments without redeploying applications. It stores feature flag state, records every toggle event, and exposes REST APIs for release workflow automation and audit tracking.

## Architecture

The project follows a clean layered backend structure:

```text
src/main/java/com/teja/featureflagservice/
  config/
    OpenApiConfig.java
  controller/
    FeatureController.java
  dto/
    ErrorResponse.java
    FeatureRequest.java
    FeatureResponse.java
    ReleaseHistoryResponse.java
    ToggleRequest.java
  entity/
    FeatureFlag.java
    ReleaseHistory.java
  exception/
    DuplicateFeatureException.java
    FeatureNotFoundException.java
    GlobalExceptionHandler.java
  repository/
    FeatureRepository.java
    ReleaseHistoryRepository.java
  service/
    FeatureService.java
  FeatureflagserviceApplication.java
```

## Technologies

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- MySQL
- Maven
- Lombok
- Bean Validation
- Swagger/OpenAPI
- JUnit 5, MockMvc, H2 for tests

## Database Setup

Create the database:

```sql
CREATE DATABASE IF NOT EXISTS feature_flag_db;
```

Optional dedicated user:

```sql
CREATE USER IF NOT EXISTS 'feature_flag_user'@'localhost' IDENTIFIED BY 'feature_flag_password';
GRANT ALL PRIVILEGES ON feature_flag_db.* TO 'feature_flag_user'@'localhost';
FLUSH PRIVILEGES;
```

The same script is available at `database/mysql-setup.sql`.

Default local datasource settings are in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/feature_flag_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

Update the username and password for your local MySQL installation before running the service.

## Run Locally

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/api-docs
```

## API Reference

### Create Feature

`POST /api/feature`

Request:

```json
{
  "featureName": "dark-mode",
  "environment": "DEV",
  "enabled": false
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "featureName": "dark-mode",
  "environment": "DEV",
  "enabled": false,
  "createdAt": "2026-05-26T05:45:00"
}
```

### Get Features By Environment

`GET /api/features/{environment}`

Example:

```bash
curl http://localhost:8080/api/features/DEV
```

Response `200 OK`:

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

### Toggle Feature

`PUT /api/toggle-feature`

Request:

```json
{
  "featureName": "dark-mode",
  "environment": "DEV",
  "enabled": true
}
```

Response `200 OK`:

```json
{
  "id": 1,
  "featureName": "dark-mode",
  "environment": "DEV",
  "enabled": true,
  "createdAt": "2026-05-26T05:45:00"
}
```

### Get Release History

`GET /api/release-history`

Response `200 OK`:

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

## Validation and Errors

Supported environments:

```text
DEV, QA, PRODUCTION
```

Example `404 Not Found` response:

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

## API Testing

Run the test suite:

```bash
./mvnw test
```

Windows:

```bash
mvnw.cmd test
```

The integration tests use H2 with the `test` profile and cover:

- Feature creation
- Environment-based feature listing
- Feature toggle workflow
- Release history audit creation
- Not found handling

Postman collection:

```text
postman/Enterprise-Feature-Flag-Service.postman_collection.json
```

## Git Commit Flow

Development was organized using professional, reviewable commits:

1. Initial Spring Boot project setup
2. Configured MySQL datasource and JPA properties
3. Added feature flag and release history entities
4. Implemented JPA repositories for feature management
5. Added DTO validation and exception handling
6. Added service layer for feature toggle workflows
7. Built REST APIs for feature flag operations
8. Added Swagger API documentation
9. Completed API testing for feature workflows
10. Completed README and API usage examples

## Future Improvements

- Add role-based access control for release managers and auditors
- Add rollback endpoints based on release history
- Add environment-level approval workflows
- Add pagination and filtering for release history
- Add Flyway or Liquibase migrations
- Add Docker Compose for MySQL and the service
- Publish OpenAPI contracts into CI
