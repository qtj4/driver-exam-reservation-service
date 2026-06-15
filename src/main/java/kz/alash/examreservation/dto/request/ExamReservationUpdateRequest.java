package kz.alash.examreservation.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import kz.alash.examreservation.entity.enums.ExamType;
import kz.alash.examreservation.entity.enums.ReservationStatus;
import lombok.Data;

@Data
public class ExamReservationUpdateRequest {

    private UUID studentId;

    private ExamType examType;

    @Future(message = "{reservation.exam-date-time.future}")
    private LocalDateTime examDateTime;

    private ReservationStatus status;
}
