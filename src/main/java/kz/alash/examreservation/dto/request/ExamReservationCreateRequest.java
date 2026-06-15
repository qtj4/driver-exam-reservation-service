package kz.alash.examreservation.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import kz.alash.examreservation.entity.enums.ExamType;
import lombok.Data;

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
