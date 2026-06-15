package kz.alash.examreservation.repository;

import java.util.Optional;
import java.util.UUID;

import kz.alash.examreservation.entity.ExamReservation;
import kz.alash.examreservation.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamReservationRepository extends JpaRepository<ExamReservation, UUID>, JpaSpecificationExecutor<ExamReservation> {

    boolean existsByStudent_IdAndStatus(UUID studentId, ReservationStatus status);

    boolean existsByStudent_IdAndStatusAndIdNot(UUID studentId, ReservationStatus status, UUID id);

    @EntityGraph(attributePaths = "student")
    @Query("select r from ExamReservation r where r.id = :id")
    Optional<ExamReservation> findByIdWithStudent(@Param("id") UUID id);

    @Override
    @EntityGraph(attributePaths = "student")
    Page<ExamReservation> findAll(Specification<ExamReservation> specification, Pageable pageable);
}
