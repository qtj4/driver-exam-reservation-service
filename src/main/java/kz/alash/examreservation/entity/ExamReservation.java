package kz.alash.examreservation.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kz.alash.examreservation.entity.enums.ExamType;
import kz.alash.examreservation.entity.enums.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

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
