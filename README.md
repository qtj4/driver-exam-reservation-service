# Driver Exam Reservation Service

REST API сервис для управления студентами и бронированиями экзамена на водительское удостоверение.

Проект позволяет:

- создавать, получать, обновлять и удалять студентов;
- создавать, получать, обновлять и удалять бронирования экзамена;
- отменять бронирование отдельным endpoint;
- фильтровать и постранично получать списки;
- проверять бизнес-правила на уровне сервиса и базы данных.

## Технологии

- Java 21
- Spring Boot 3.5.8
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL 16
- Liquibase
- SpringDoc OpenAPI / Swagger UI
- Lombok
- MapStruct
- Maven
- Docker и Docker Compose
- JUnit 5, Mockito, Testcontainers

## Запуск через Docker

Требования:

- Docker
- Docker Compose

Запуск проекта:

```bash
docker compose up --build
```

Остановка:

```bash
docker compose down
```

Остановка с удалением данных PostgreSQL:

```bash
docker compose down -v
```

После запуска доступны:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health check: http://localhost:8080/actuator/health

## Конфигурация базы данных

В Docker Compose поднимается PostgreSQL с такими параметрами:

| Параметр | Значение |
| --- | --- |
| Database | `driver_exam_reservation_db` |
| User | `postgres` |
| Password | `postgres` |
| Port | `5432` |

Приложение читает настройки из environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/driver_exam_reservation_db` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `APP_PORT` | `8080` |
| `LIQUIBASE_ENABLED` | `true` |
| `SHOW_SQL` | `false` |

Схема базы создается через Liquibase. Hibernate настроен в режиме `ddl-auto: validate`, то есть он не создает таблицы сам, а только проверяет соответствие Entity и схемы.

## API

Base URL:

```text
http://localhost:8080
```

### Students

| Method | Path | Описание |
| --- | --- | --- |
| `POST` | `/api/v1/students` | Создать студента |
| `GET` | `/api/v1/students/{id}` | Получить студента по id |
| `GET` | `/api/v1/students` | Получить список студентов |
| `PUT` | `/api/v1/students/{id}` | Обновить студента |
| `DELETE` | `/api/v1/students/{id}` | Удалить студента |

Пример фильтрации студентов:

```text
/api/v1/students?page=0&size=10&sort=createdAt,desc&search=ayan
/api/v1/students?iin=910101123456
```

### Exam Reservations

| Method | Path | Описание |
| --- | --- | --- |
| `POST` | `/api/v1/exam-reservations` | Создать бронирование |
| `GET` | `/api/v1/exam-reservations/{id}` | Получить бронирование по id |
| `GET` | `/api/v1/exam-reservations` | Получить список бронирований |
| `PUT` | `/api/v1/exam-reservations/{id}` | Обновить бронирование |
| `PATCH` | `/api/v1/exam-reservations/{id}/cancel` | Отменить бронирование |
| `DELETE` | `/api/v1/exam-reservations/{id}` | Удалить бронирование |

Пример фильтрации бронирований:

```text
/api/v1/exam-reservations?studentId=<uuid>&examType=THEORY&status=ACTIVE
/api/v1/exam-reservations?from=2026-06-16T09:00:00&to=2026-06-30T18:00:00
```

## Примеры запросов

Создание студента:

```json
{
  "iin": "910101123456",
  "firstName": "Ayan",
  "lastName": "Karimov",
  "phone": "+77770000001"
}
```

Создание бронирования:

```json
{
  "studentId": "550e8400-e29b-41d4-a716-446655440000",
  "examType": "THEORY",
  "examDateTime": "2026-06-16T09:00:00"
}
```

При создании бронирования `status` не передается клиентом. Сервис автоматически устанавливает `ACTIVE`.

## Бизнес-правила

- `iin` обязателен, уникален и должен содержать ровно 12 цифр.
- `firstName`, `lastName`, `phone` обязательны.
- Студент должен существовать перед созданием бронирования.
- Нельзя создать бронирование на дату и время в прошлом.
- У одного студента может быть только одно активное бронирование.
- Новое бронирование всегда создается со статусом `ACTIVE`.
- Бронирование со статусом `COMPLETED` нельзя отменить.

Правило "одно ACTIVE бронирование" защищено дважды:

- в сервисе, чтобы вернуть понятную ошибку клиенту;
- в PostgreSQL через partial unique index, чтобы защититься от параллельных запросов.

## Формат ошибок

Ошибки возвращаются централизованно через `@RestControllerAdvice`.

Пример validation error:

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

Поддерживаются языки `ru`, `en`, `kz` через header:

```http
Accept-Language: ru
```

По умолчанию используется русский язык.

## Swagger

Swagger UI доступен по адресу:

```text
http://localhost:8080/swagger-ui/index.html
```

В Swagger описаны endpoints, query parameters, request examples и основные response codes.

## Postman

Коллекция находится здесь:

```text
postman/driver-exam-reservation.postman_collection.json
```

В коллекции есть сценарии для:

- health check;
- создания студента;
- проверки validation errors;
- создания бронирования;
- проверки duplicate active reservation;
- отмены бронирования;
- удаления записей.

## Локальные проверки

Компиляция:

```bash
mvn compile
```

Запуск тестов:

```bash
mvn test
```

Проверка стиля:

```bash
mvn checkstyle:check
```

Integration tests используют Testcontainers и поднимают реальный PostgreSQL. Для их запуска Docker должен быть запущен.

