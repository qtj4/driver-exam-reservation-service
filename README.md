# Driver Exam Reservation Service

REST API for creating students and booking driving license exams for them.

## Technologies

- Java 21
- Spring Boot 3.5.8
- Spring Web, Spring Data JPA, Validation, Actuator
- PostgreSQL 16
- Liquibase
- SpringDoc OpenAPI 2.8.14
- Lombok 1.18.36
- MapStruct 1.6.3
- Maven
- Docker and Docker Compose
- JUnit 5, Mockito, AssertJ, Testcontainers

## Requirements

- Docker
- Docker Compose
- Optional for local development: JDK 21 and Maven

## Run

```bash
docker compose up --build
```

Stop:

```bash
docker compose down
```

Reset database:

```bash
docker compose down -v
```

Useful URLs:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health

## Database Configuration

The application reads these environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/driver_exam_reservation_db` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `APP_PORT` | `8080` |
| `LIQUIBASE_ENABLED` | `true` |
| `SHOW_SQL` | `false` |

Docker Compose starts PostgreSQL with database `driver_exam_reservation_db`, user `postgres`, and password `postgres`.

## API

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/v1/students` | Create student |
| `GET` | `/api/v1/students/{id}` | Get student by id |
| `GET` | `/api/v1/students` | List students with pagination and filters |
| `PUT` | `/api/v1/students/{id}` | Update student |
| `DELETE` | `/api/v1/students/{id}` | Delete student |
| `POST` | `/api/v1/exam-reservations` | Create exam reservation |
| `GET` | `/api/v1/exam-reservations/{id}` | Get reservation by id |
| `GET` | `/api/v1/exam-reservations` | List reservations with pagination and filters |
| `PUT` | `/api/v1/exam-reservations/{id}` | Update reservation |
| `PATCH` | `/api/v1/exam-reservations/{id}/cancel` | Cancel reservation |
| `DELETE` | `/api/v1/exam-reservations/{id}` | Delete reservation |

Student filters:

```text
/api/v1/students?page=0&size=10&sort=createdAt,desc&search=ivan&iin=123456789012
```

Reservation filters:

```text
/api/v1/exam-reservations?studentId=<uuid>&examType=THEORY&status=ACTIVE&from=2026-06-16T09:00:00&to=2026-06-30T18:00:00
```

## Business Rules

- Student IIN is required, unique, and must contain exactly 12 digits.
- A student must exist before creating or moving a reservation to that student.
- A reservation cannot be created for a past date and time.
- One student can have only one `ACTIVE` reservation.
- New reservations are always created with status `ACTIVE`.
- A reservation with status `COMPLETED` cannot be cancelled.

## Error Format

```json
{
  "error": "VALIDATION_ERROR",
  "status": 400,
  "message": "Data validation failed",
  "fieldErrors": {
    "iin": "IIN must contain exactly 12 digits"
  },
  "timestamp": "2026-06-15T12:00:00"
}
```

Localization is available with the `Accept-Language` header: `ru`, `en`, and `kz`. The default locale is `ru`.

## Postman

Import these files into Postman:

- `postman/driver-exam-reservation.postman_collection.json`
- `postman/local.postman_environment.json`

The collection stores `studentId` and `reservationId` from successful create responses and includes success, validation,
not found, business rule, pagination, filtering, cancel, and delete scenarios.

## Local Checks

Compile:

```bash
mvn compile
```

Run tests:

```bash
mvn test
```

Run linter:

```bash
mvn checkstyle:check
```
