package kz.alash.examreservation.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kz.alash.examreservation.dto.request.ExamReservationCreateRequest;
import kz.alash.examreservation.dto.request.ExamReservationUpdateRequest;
import kz.alash.examreservation.dto.response.ExamReservationResponse;
import kz.alash.examreservation.entity.enums.ExamType;
import kz.alash.examreservation.entity.enums.ReservationStatus;
import kz.alash.examreservation.service.ExamReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exam-reservations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Exam Reservations", description = "API for managing exam reservations")
public class ExamReservationController {

    private final ExamReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create exam reservation")
    public ExamReservationResponse create(@Valid @RequestBody ExamReservationCreateRequest request) {
        return reservationService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exam reservation by id")
    public ExamReservationResponse getById(@PathVariable UUID id) {
        return reservationService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Get exam reservations with pagination and filters")
    public Page<ExamReservationResponse> getAll(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) ExamType examType,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "{page.number.min}") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "{page.size.min}")
            @Max(value = 100, message = "{page.size.max}") int size,
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Sort sort
    ) {
        return reservationService.getAll(studentId, examType, status, from, to, PageRequest.of(page, size, sort));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update exam reservation")
    public ExamReservationResponse update(@PathVariable UUID id, @Valid @RequestBody ExamReservationUpdateRequest request) {
        return reservationService.update(id, request);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel exam reservation")
    public ExamReservationResponse cancel(@PathVariable UUID id) {
        return reservationService.cancel(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete exam reservation")
    public void delete(@PathVariable UUID id) {
        reservationService.delete(id);
    }
}
