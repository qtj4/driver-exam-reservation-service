package kz.alash.examreservation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import kz.alash.examreservation.entity.enums.ExamType;
import kz.alash.examreservation.entity.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamReservationResponse {

    private UUID id;

    private UUID studentId;

    private ExamType examType;

    private LocalDateTime examDateTime;

    private ReservationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
