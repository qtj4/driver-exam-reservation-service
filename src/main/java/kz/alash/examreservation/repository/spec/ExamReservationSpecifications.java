package kz.alash.examreservation.repository.spec;

import java.time.LocalDateTime;
import java.util.UUID;

import kz.alash.examreservation.entity.ExamReservation;
import kz.alash.examreservation.entity.enums.ExamType;
import kz.alash.examreservation.entity.enums.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ExamReservationSpecifications {

    private ExamReservationSpecifications() {
    }

    public static Specification<ExamReservation> byFilters(
            UUID studentId,
            ExamType examType,
            ReservationStatus status,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return Specification.allOf(
                studentIdEquals(studentId),
                examTypeEquals(examType),
                statusEquals(status),
                from(from),
                to(to)
        );
    }

    private static Specification<ExamReservation> studentIdEquals(UUID studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("student").get("id"), studentId);
        };
    }

    private static Specification<ExamReservation> examTypeEquals(ExamType examType) {
        return (root, query, criteriaBuilder) -> {
            if (examType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("examType"), examType);
        };
    }

    private static Specification<ExamReservation> statusEquals(ReservationStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    private static Specification<ExamReservation> from(LocalDateTime from) {
        return (root, query, criteriaBuilder) -> {
            if (from == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("examDateTime"), from);
        };
    }

    private static Specification<ExamReservation> to(LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            if (to == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("examDateTime"), to);
        };
    }
}
