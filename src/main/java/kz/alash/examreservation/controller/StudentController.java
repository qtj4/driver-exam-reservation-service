package kz.alash.examreservation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kz.alash.examreservation.dto.request.StudentCreateRequest;
import kz.alash.examreservation.dto.request.StudentUpdateRequest;
import kz.alash.examreservation.dto.response.StudentResponse;
import kz.alash.examreservation.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Validated
@Tag(name = "Students", description = "API for managing students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create student")
    public StudentResponse create(@Valid @RequestBody StudentCreateRequest request) {
        return studentService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by id")
    public StudentResponse getById(@PathVariable UUID id) {
        return studentService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Get students with pagination and filters")
    public Page<StudentResponse> getAll(
            @RequestParam(required = false) String iin,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "{page.number.min}") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "{page.size.min}")
            @Max(value = 100, message = "{page.size.max}") int size,
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Sort sort
    ) {
        return studentService.getAll(iin, firstName, lastName, phone, search, PageRequest.of(page, size, sort));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student")
    public StudentResponse update(@PathVariable UUID id, @Valid @RequestBody StudentUpdateRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete student")
    public void delete(@PathVariable UUID id) {
        studentService.delete(id);
    }
}
