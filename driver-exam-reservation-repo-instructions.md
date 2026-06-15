# Driver Exam Reservation Service - Repo And Start Instructions

This file is for the project owner and for the person testing the submitted project.

## For The Owner

Create the project in a clean folder, not inside `enki-core`, unless you intentionally want a monorepo module.

Recommended repository name:

```bash
driver-exam-reservation-service
```

Initialize Git:

```bash
git init
git branch -M main
git add .
git commit -m "Initial driver exam reservation service"
git remote add origin <your-git-repository-url>
git push -u origin main
```

Before submitting, run:

```bash
mvn test
docker compose up --build
```

Then check:

```text
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui/index.html
```

Stop the project:

```bash
docker compose down
```

Reset the database if needed:

```bash
docker compose down -v
```

## What To Submit

Submit the Git repository link. The repository must contain:

- Source code.
- `README.md`.
- `Dockerfile`.
- `docker-compose.yml`.
- Liquibase migrations.
- Unit tests.
- Swagger/OpenAPI setup.
- Postman collection under `postman/`.
- These startup instructions, either in README or a separate markdown file.

## For The Tester

Prerequisites:

- Docker.
- Docker Compose.
- Free local port `8080`.
- Free local port `5432`, or change the mapped PostgreSQL port in `docker-compose.yml`.

Start:

```bash
git clone <repository-url>
cd driver-exam-reservation-service
docker compose up --build
```

Wait until the application starts successfully. Then open:

```text
Swagger UI: http://localhost:8080/swagger-ui/index.html
Health:     http://localhost:8080/actuator/health
```

Import Postman files:

- `postman/driver-exam-reservation.postman_collection.json`
- `postman/local.postman_environment.json`

Recommended manual test flow:

1. Create a student with a valid 12-digit IIN.
2. List students and verify the created student appears.
3. Create an exam reservation for that student with a future `examDateTime`.
4. Try to create a second active reservation for the same student and expect `400`.
5. Try to create a reservation in the past and expect `400`.
6. Cancel the active reservation.
7. Create another reservation for the same student and verify it succeeds after cancellation.
8. Test validation with `Accept-Language: ru`, `Accept-Language: en`, and `Accept-Language: kz`.

Stop:

```bash
docker compose down
```

Remove containers and database volume:

```bash
docker compose down -v
```

## API Summary

Base URL:

```text
http://localhost:8080
```

Student endpoints:

```text
POST   /api/v1/students
GET    /api/v1/students/{id}
GET    /api/v1/students
PUT    /api/v1/students/{id}
DELETE /api/v1/students/{id}
```

Reservation endpoints:

```text
POST   /api/v1/exam-reservations
GET    /api/v1/exam-reservations/{id}
GET    /api/v1/exam-reservations
PUT    /api/v1/exam-reservations/{id}
PATCH  /api/v1/exam-reservations/{id}/cancel
DELETE /api/v1/exam-reservations/{id}
```

Useful query parameters:

```text
/api/v1/students?page=0&size=10&search=ivan
/api/v1/students?iin=123456789012
/api/v1/exam-reservations?studentId=<uuid>&status=ACTIVE
/api/v1/exam-reservations?examType=THEORY&from=2026-06-16T09:00:00&to=2026-06-30T18:00:00
```

## Notes On Style

This project should feel similar to Enki Core, but smaller:

- Same Java/Spring/Liquibase/PostgreSQL direction.
- Same centralized error handling idea.
- Same 3-language message bundles.
- Same Swagger annotations in controllers.
- Same MapStruct usage.
- Same `createdAt` and `updatedAt` audit fields.

But avoid Enki-specific infrastructure:

- No Keycloak/security permissions.
- No Kafka.
- No Redis.
- No MinIO.
- No API Gateway.
- No generic CRUD service interfaces.
- No generic repository wrappers.
- No soft delete unless the evaluator explicitly asks for it.
