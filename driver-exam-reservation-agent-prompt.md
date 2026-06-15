# Driver Exam Reservation Service - Agent Prompt

Use this prompt for an agentic AI that must create a standalone REST API project in the same practical style as `enki-core`.

## Context From Enki Core

The current project is a Java/Spring multi-module backend. Match the useful conventions, but keep this assignment smaller.

Use these versions and patterns:

- Java 21.
- Maven.
- Spring Boot `3.5.8`.
- Spring Web, Spring Data JPA, Validation, PostgreSQL.
- Liquibase with a YAML master changelog and SQL migration files.
- SpringDoc OpenAPI `2.8.14`.
- Lombok `1.18.36`.
- MapStruct `1.6.3`.
- JUnit 5, Mockito, AssertJ, Spring Boot Test, Testcontainers `1.21.4` where integration tests are useful.
- PostgreSQL `16-alpine` in Docker Compose.
- Controllers return DTOs directly. Avoid `ResponseEntity` unless there is a concrete reason.
- Use `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`, `@Validated`, `@Valid @RequestBody`, Swagger `@Tag` and `@Operation`.
- Use `Page<T>` for paginated endpoints.
- Use `@SortDefault`, `PageRequest`, and query parameters for filtering.
- Use centralized `@RestControllerAdvice`.
- Error response shape should follow Enki style:

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

Important simplification:

- Do not copy Enki's generic `CrudService`, `ReadDeleteRepository`, `CoreMapper`, audit helper, Kafka, Redis, security starter, permissions, or soft delete patterns into this assignment.
- Use concrete services such as `StudentService` and `ExamReservationService`.
- Use normal Spring Data `JpaRepository` and `JpaSpecificationExecutor`.
- Use a small `BaseEntity` only for `id`, `createdAt`, and `updatedAt`. This gives the audit fields without bringing in security/auditor complexity.
- Use hard delete unless the assignment explicitly requires soft delete.

## Goal

Create a REST API for booking driving license exams. The service must allow users to create students and reserve exams for them.

The project must run with:

```bash
docker compose up --build
```

After startup, testers must be able to manage students and their exam reservations through REST API, Swagger UI, and the Postman collection.

## Project Structure

Create a standalone project with this shape:

```text
driver-exam-reservation-service/
  pom.xml
  Dockerfile
  docker-compose.yml
  README.md
  postman/
    driver-exam-reservation.postman_collection.json
    local.postman_environment.json
  src/
    main/
      java/kz/alash/examreservation/
        DriverExamReservationApplication.java
        config/
          OpenApiConfig.java
          MessageResourceConfig.java
          JpaAuditConfig.java
        controller/
          StudentController.java
          ExamReservationController.java
        dto/
          request/
            StudentCreateRequest.java
            StudentUpdateRequest.java
            ExamReservationCreateRequest.java
            ExamReservationUpdateRequest.java
          response/
            StudentResponse.java
            ExamReservationResponse.java
            ErrorResponse.java
        entity/
          BaseEntity.java
          Student.java
          ExamReservation.java
          enums/
            ExamType.java
            ReservationStatus.java
        exception/
          BusinessException.java
          NotFoundException.java
        handler/
          GlobalExceptionHandler.java
        mapper/
          StudentMapper.java
          ExamReservationMapper.java
        repository/
          StudentRepository.java
          ExamReservationRepository.java
          spec/
            StudentSpecifications.java
            ExamReservationSpecifications.java
        service/
          StudentService.java
          ExamReservationService.java
    main/resources/
      application.yml
      messages_en.properties
      messages_ru.properties
      messages_kz.properties
      validation_en.properties
      validation_ru.properties
      validation_kz.properties
      db/changelog/
        db.changelog-master.yaml
        sql/
          202606151200_create_student_and_exam_reservation_tables.sql
    test/
      java/kz/alash/examreservation/
        service/
          StudentServiceTest.java
          ExamReservationServiceTest.java
        controller/
          StudentControllerTest.java
          ExamReservationControllerTest.java
```

## Maven

Use Spring Boot parent and centralized properties.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.8</version>
</parent>

<properties>
    <java.version>21</java.version>
    <mapstruct.version>1.6.3</mapstruct.version>
    <springdoc.version>2.8.14</springdoc.version>
    <testcontainers.version>1.21.4</testcontainers.version>
    <lombok.version>1.18.36</lombok.version>
</properties>
```

Required dependencies:

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `org.liquibase:liquibase-core`
- `org.postgresql:postgresql` with runtime scope
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `org.mapstruct:mapstruct`
- `org.projectlombok:lombok` with provided/optional scope
- `spring-boot-starter-test`
- `org.mockito:mockito-junit-jupiter` if not already pulled by Boot test
- `org.testcontainers:postgresql`
- `org.testcontainers:junit-jupiter`

Configure `maven-compiler-plugin` annotation processors for Lombok, MapStruct, and `lombok-mapstruct-binding`.

## Entities

Use UUID ids to match Enki. If a strict external checker requires numeric ids, switch all ids to `Long` consistently.

### BaseEntity

Use a small base entity. Do not add `createdBy`, `updatedBy`, `deletedBy`, or `isDeleted`.

```java
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

Add:

```java
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
```

### Student

Fields:

- `id`
- `iin`
- `firstName`
- `lastName`
- `phone`
- `createdAt`
- `updatedAt`

Rules:

- `iin` required, exactly 12 digits, unique.
- `firstName` required.
- `lastName` required.
- `phone` required.

Entity:

```java
@Entity
@Table(
        name = "student",
        indexes = {
                @Index(name = "student_iin_index", columnList = "iin", unique = true),
                @Index(name = "student_last_name_index", columnList = "last_name")
        }
)
@Getter
@Setter
public class Student extends BaseEntity {

    @Column(name = "iin", nullable = false, length = 12, unique = true)
    private String iin;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ExamReservation> reservations = new ArrayList<>();
}
```

### ExamReservation

Fields:

- `id`
- `studentId`
- `examType`
- `examDateTime`
- `status`
- `createdAt`
- `updatedAt`

Entity:

```java
@Entity
@Table(
        name = "exam_reservation",
        indexes = {
                @Index(name = "exam_reservation_student_id_index", columnList = "student_id"),
                @Index(name = "exam_reservation_status_index", columnList = "status"),
                @Index(name = "exam_reservation_exam_date_time_index", columnList = "exam_date_time")
        }
)
@Getter
@Setter
public class ExamReservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 32)
    private ExamType examType;

    @Column(name = "exam_date_time", nullable = false)
    private LocalDateTime examDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReservationStatus status = ReservationStatus.ACTIVE;
}
```

Enums:

```java
public enum ExamType {
    THEORY,
    PRACTICE
}

public enum ReservationStatus {
    ACTIVE,
    CANCELLED,
    COMPLETED
}
```

N+1 rules:

- Keep associations lazy.
- Do not include `reservations` inside `StudentResponse`.
- For reservation responses, return `studentId`, not the whole `Student`.
- If a list endpoint needs student details later, use `@EntityGraph(attributePaths = "student")` or a DTO projection.

## DTOs And Validation

Use request/response DTOs. Do not expose entities directly.

Student create/update request:

```java
@Data
public class StudentCreateRequest {

    @NotBlank(message = "{student.iin.required}")
    @Pattern(regexp = "\\d{12}", message = "{student.iin.invalid}")
    private String iin;

    @NotBlank(message = "{student.first-name.required}")
    private String firstName;

    @NotBlank(message = "{student.last-name.required}")
    private String lastName;

    @NotBlank(message = "{student.phone.required}")
    private String phone;
}
```

Reservation create request:

```java
@Data
public class ExamReservationCreateRequest {

    @NotNull(message = "{reservation.student-id.required}")
    private UUID studentId;

    @NotNull(message = "{reservation.exam-type.required}")
    private ExamType examType;

    @NotNull(message = "{reservation.exam-date-time.required}")
    @Future(message = "{reservation.exam-date-time.future}")
    private LocalDateTime examDateTime;
}
```

Create response DTOs with `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor` where useful.

`ExamReservationResponse` must contain:

- `UUID id`
- `UUID studentId`
- `ExamType examType`
- `LocalDateTime examDateTime`
- `ReservationStatus status`
- `LocalDateTime createdAt`
- `LocalDateTime updatedAt`

## Repositories

Use standard Spring Data repositories.

```java
@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    boolean existsByIin(String iin);
    boolean existsByIinAndIdNot(String iin, UUID id);
}
```

```java
@Repository
public interface ExamReservationRepository extends JpaRepository<ExamReservation, UUID>, JpaSpecificationExecutor<ExamReservation> {

    boolean existsByStudent_IdAndStatus(UUID studentId, ReservationStatus status);

    boolean existsByStudent_IdAndStatusAndIdNot(UUID studentId, ReservationStatus status, UUID id);

    @EntityGraph(attributePaths = "student")
    @Query("select r from ExamReservation r where r.id = :id")
    Optional<ExamReservation> findByIdWithStudent(@Param("id") UUID id);
}
```

For optional filters, use small Specification helper classes. Do not put large query logic in controllers.

Student filters:

- `iin`
- `firstName`
- `lastName`
- `phone`
- `search` across iin, firstName, lastName, phone

Reservation filters:

- `studentId`
- `examType`
- `status`
- `from`
- `to`

## Services

Use concrete services. Mark read-only query methods with `@Transactional(readOnly = true)` and write methods with `@Transactional`.

Create these business exceptions:

```java
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

Business rules:

1. Student must exist before creating or updating a reservation.
2. Reservation cannot be created for date/time in the past.
3. One student can have only one `ACTIVE` reservation.
4. Reservation with status `COMPLETED` cannot be cancelled.
5. New reservation status is always `ACTIVE`, ignoring any client-sent status.

Recommended `ExamReservationService` create/cancel shape:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ExamReservationService {

    private final ExamReservationRepository reservationRepository;
    private final StudentRepository studentRepository;
    private final ExamReservationMapper mapper;
    private final MessageSource messageSource;
    private final Clock clock;

    @Transactional
    public ExamReservationResponse create(ExamReservationCreateRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new NotFoundException(message("student.not-found")));

        if (!request.getExamDateTime().isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException(message("reservation.date-time.past"));
        }

        if (reservationRepository.existsByStudent_IdAndStatus(student.getId(), ReservationStatus.ACTIVE)) {
            throw new BusinessException(message("reservation.active.already-exists"));
        }

        ExamReservation reservation = mapper.toEntity(request);
        reservation.setStudent(student);
        reservation.setStatus(ReservationStatus.ACTIVE);

        return mapper.toDto(reservationRepository.save(reservation));
    }

    @Transactional
    public ExamReservationResponse cancel(UUID id) {
        ExamReservation reservation = reservationRepository.findByIdWithStudent(id)
                .orElseThrow(() -> new NotFoundException(message("reservation.not-found")));

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException(message("reservation.completed.cannot-cancel"));
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return mapper.toDto(reservation);
    }

    private String message(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
```

Add a simple `Clock` bean for testable time validation:

```java
@Bean
public Clock clock() {
    return Clock.systemDefaultZone();
}
```

## Mappers

Use MapStruct, no manual mapping unless MapStruct makes a case harder.

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamReservationMapper {

    @Mapping(target = "studentId", source = "student.id")
    ExamReservationResponse toDto(ExamReservation entity);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "status", ignore = true)
    ExamReservation toEntity(ExamReservationCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ExamReservationUpdateRequest request, @MappingTarget ExamReservation entity);
}
```

## Controllers

Base paths:

- `/api/v1/students`
- `/api/v1/exam-reservations`

Student endpoints:

- `POST /api/v1/students`
- `GET /api/v1/students/{id}`
- `GET /api/v1/students?page=0&size=10&sort=createdAt,desc&search=...&iin=...`
- `PUT /api/v1/students/{id}`
- `DELETE /api/v1/students/{id}`

Reservation endpoints:

- `POST /api/v1/exam-reservations`
- `GET /api/v1/exam-reservations/{id}`
- `GET /api/v1/exam-reservations?page=0&size=10&studentId=...&examType=THEORY&status=ACTIVE&from=2026-06-16T09:00:00&to=2026-06-30T18:00:00`
- `PUT /api/v1/exam-reservations/{id}`
- `DELETE /api/v1/exam-reservations/{id}`
- `PATCH /api/v1/exam-reservations/{id}/cancel`

Controller style:

```java
@RestController
@RequestMapping("/api/v1/exam-reservations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Exam Reservations", description = "API for managing exam reservations")
public class ExamReservationController {

    private final ExamReservationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create exam reservation")
    public ExamReservationResponse create(@Valid @RequestBody ExamReservationCreateRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel exam reservation")
    public ExamReservationResponse cancel(@PathVariable UUID id) {
        return service.cancel(id);
    }
}
```

## Error Handling And Localization

Create `MessageResourceConfig` similar in spirit to Enki but standalone.

```java
@Configuration
public class MessageResourceConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasenames("classpath:messages", "classpath:validation");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setDefaultLocale(Locale.forLanguageTag("ru"));
        return source;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }
}
```

`GlobalExceptionHandler` must handle at minimum:

- `NotFoundException` -> 404, `NOT_FOUND`
- `BusinessException` -> 400, `BUSINESS_RULE_VIOLATION`
- `MethodArgumentNotValidException` -> 400, `VALIDATION_ERROR`, with `fieldErrors`
- `ConstraintViolationException` -> 400, `VALIDATION_ERROR`
- `DataIntegrityViolationException` -> 400 or 409 with localized user-friendly message
- fallback `Exception` -> 500, `INTERNAL_SERVER_ERROR`

Use `Accept-Language` for `ru`, `en`, `kz`. Default locale is `ru`, matching Enki.

Validation messages must exist in 3 files:

- `validation_ru.properties`
- `validation_en.properties`
- `validation_kz.properties`

Business/error messages must exist in 3 files:

- `messages_ru.properties`
- `messages_en.properties`
- `messages_kz.properties`

Minimum message keys:

```properties
validation.failed=...
error.unexpected=...
database.error=...
student.not-found=...
student.iin.already-exists=...
reservation.not-found=...
reservation.date-time.past=...
reservation.active.already-exists=...
reservation.completed.cannot-cancel=...
student.iin.required=...
student.iin.invalid=...
student.first-name.required=...
student.last-name.required=...
student.phone.required=...
reservation.student-id.required=...
reservation.exam-type.required=...
reservation.exam-date-time.required=...
reservation.exam-date-time.future=...
page.number.min=...
page.size.min=...
page.size.max=...
```

## Liquibase

Use Enki style:

`src/main/resources/db/changelog/db.changelog-master.yaml`

```yaml
databaseChangeLog:
  - includeAll:
      path: db/changelog/sql
```

Initial SQL migration:

```sql
create table student
(
    id uuid not null
        constraint student_pk primary key,
    iin varchar(12) not null,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    phone varchar(255) not null,
    created_at timestamp without time zone not null,
    updated_at timestamp without time zone not null,
    constraint student_iin_digits_check check (iin ~ '^[0-9]{12}$')
);

create unique index student_iin_uindex on student (iin);
create index student_last_name_index on student (last_name);

create table exam_reservation
(
    id uuid not null
        constraint exam_reservation_pk primary key,
    student_id uuid not null,
    exam_type varchar(32) not null,
    exam_date_time timestamp without time zone not null,
    status varchar(32) not null,
    created_at timestamp without time zone not null,
    updated_at timestamp without time zone not null,
    constraint exam_reservation_student_fk
        foreign key (student_id) references student (id) on delete cascade,
    constraint exam_reservation_exam_type_check
        check (exam_type in ('THEORY', 'PRACTICE')),
    constraint exam_reservation_status_check
        check (status in ('ACTIVE', 'CANCELLED', 'COMPLETED'))
);

create index exam_reservation_student_id_index on exam_reservation (student_id);
create index exam_reservation_status_index on exam_reservation (status);
create index exam_reservation_exam_date_time_index on exam_reservation (exam_date_time);
create unique index exam_reservation_one_active_per_student_uindex
    on exam_reservation (student_id)
    where status = 'ACTIVE';
```

The partial unique index is important. It protects the "one active reservation" rule even during concurrent requests.

## Application Configuration

Use `application.yml`:

```yaml
server:
  port: ${APP_PORT:8080}

spring:
  threads:
    virtual:
      enabled: true
  application:
    name: driver-exam-reservation-service
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/driver_exam_reservation_db}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    hikari:
      minimum-idle: 2
      maximum-pool-size: 10
      connection-timeout: 30000
  jpa:
    open-in-view: false
    show-sql: ${SHOW_SQL:false}
  liquibase:
    enabled: ${LIQUIBASE_ENABLED:true}
    change-log: classpath:db/changelog/db.changelog-master.yaml
  jackson:
    default-property-inclusion: non_null

management:
  endpoints:
    web:
      exposure:
        include: health,info

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    operations-sorter: method
    tags-sorter: alpha

logging:
  level:
    root: info
```

## Docker

Create a multi-stage Dockerfile:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Create `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: driver-exam-postgres
    environment:
      POSTGRES_DB: driver_exam_reservation_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d driver_exam_reservation_db"]
      interval: 5s
      timeout: 5s
      retries: 10

  app:
    build: .
    container_name: driver-exam-reservation-service
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/driver_exam_reservation_db
      DB_USER: postgres
      DB_PASSWORD: postgres
      APP_PORT: 8080
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

No Kafka, Redis, MinIO, Keycloak, or API gateway for this assignment.

## Tests

Prioritize service tests for business behavior.

Minimum `ExamReservationServiceTest` cases:

- Create reservation successfully.
- Create fails when student does not exist.
- Create fails when date/time is in the past.
- Create fails when student already has an active reservation.
- Create sets status to `ACTIVE`.
- Cancel active reservation successfully.
- Cancel completed reservation fails.
- Update to active fails if another active reservation exists for the same student.
- Get by id fails with `NotFoundException`.

Minimum `StudentServiceTest` cases:

- Create student successfully.
- Create fails when IIN already exists.
- Update fails when IIN belongs to another student.
- Get by id fails when not found.
- Filter/search returns expected results if using integration tests.

Controller validation tests:

- Invalid IIN returns 400 with `fieldErrors.iin`.
- Missing required field returns 400.
- Invalid page/size returns 400.

Use Mockito tests for business rules. Use Testcontainers only if adding repository/specification integration tests.

## Postman Collection

Create `postman/driver-exam-reservation.postman_collection.json` and environment file.

Collection must include:

- Health check: `GET /actuator/health`
- Swagger docs check: `GET /v3/api-docs`
- Create student success.
- Create student invalid IIN.
- Get student by id.
- List students with pagination/search.
- Update student.
- Delete student.
- Create reservation success.
- Create reservation for missing student.
- Create reservation in the past.
- Create duplicate active reservation.
- Get reservation by id.
- List reservations with `studentId`, `examType`, `status`, `from`, `to`.
- Update reservation.
- Cancel reservation.
- Cancel completed reservation or a prepared request that explains how to create completed status first.
- Delete reservation.

Use collection variables:

- `baseUrl=http://localhost:8080`
- `studentId`
- `reservationId`

Add Postman tests that store ids from successful create responses.

## README

README must include:

- Project purpose.
- Technologies and versions.
- Requirements: Docker, Docker Compose, optional JDK 21/Maven for local run.
- Run command: `docker compose up --build`.
- Stop command: `docker compose down`.
- Reset database command: `docker compose down -v`.
- Swagger URL: `http://localhost:8080/swagger-ui/index.html`.
- Health URL: `http://localhost:8080/actuator/health`.
- Database config env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `APP_PORT`.
- API endpoint table.
- Business rules.
- Validation/error format with example.
- Postman import instructions.
- Test command: `mvn test`.

## Final Acceptance Checklist

Before finishing, verify:

- `mvn test` passes.
- `docker compose up --build` starts app and PostgreSQL.
- Liquibase creates both tables and indexes.
- Swagger UI opens.
- Postman collection can run the main happy path.
- Validation errors are localized in `ru`, `en`, and `kz` using `Accept-Language`.
- No entities are returned directly from controllers.
- `Student` to `ExamReservation` relation is lazy.
- Reservation list does not trigger N+1 queries.
- New reservation status is always `ACTIVE`.
- One-active-reservation rule is enforced in service and database.
