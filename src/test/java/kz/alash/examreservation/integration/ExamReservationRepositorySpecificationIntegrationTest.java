package kz.alash.examreservation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import kz.alash.examreservation.config.JpaAuditConfig;
import kz.alash.examreservation.entity.ExamReservation;
import kz.alash.examreservation.entity.Student;
import kz.alash.examreservation.entity.enums.ExamType;
import kz.alash.examreservation.entity.enums.ReservationStatus;
import kz.alash.examreservation.repository.ExamReservationRepository;
import kz.alash.examreservation.repository.StudentRepository;
import kz.alash.examreservation.repository.spec.ExamReservationSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditConfig.class)
class ExamReservationRepositorySpecificationIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final LocalDateTime BASE_DATE_TIME = LocalDateTime.of(2027, 1, 10, 9, 0);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamReservationRepository reservationRepository;

    private Student ayan;

    private Student dana;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        studentRepository.deleteAll();

        ayan = studentRepository.saveAndFlush(student("940404444444", "Ayan", "Karimov", "+77770000004"));
        dana = studentRepository.saveAndFlush(student("950505555555", "Dana", "Omarova", "+77770000005"));

        reservationRepository.saveAllAndFlush(List.of(
                reservation(ayan, ExamType.THEORY, BASE_DATE_TIME, ReservationStatus.ACTIVE),
                reservation(ayan, ExamType.PRACTICE, BASE_DATE_TIME.plusDays(2), ReservationStatus.CANCELLED),
                reservation(dana, ExamType.THEORY, BASE_DATE_TIME.plusDays(5), ReservationStatus.COMPLETED)
        ));
    }

    @Test
    void filtersReservationsByStudentIdExamTypeStatusAndDateRange() {
        Page<ExamReservation> page = reservationRepository.findAll(
                ExamReservationSpecifications.byFilters(
                        ayan.getId(),
                        ExamType.PRACTICE,
                        ReservationStatus.CANCELLED,
                        BASE_DATE_TIME.plusDays(1),
                        BASE_DATE_TIME.plusDays(3)
                ),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getStudent().getId()).isEqualTo(ayan.getId());
        assertThat(page.getContent().getFirst().getExamType()).isEqualTo(ExamType.PRACTICE);
        assertThat(page.getContent().getFirst().getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void statusFilterReturnsOnlyActiveReservation() {
        Page<ExamReservation> page = reservationRepository.findAll(
                ExamReservationSpecifications.byFilters(null, null, ReservationStatus.ACTIVE, null, null),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getStudent().getId()).isEqualTo(ayan.getId());
        assertThat(page.getContent().getFirst().getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void dateRangeFilterReturnsReservationsInsideInclusiveRange() {
        Page<ExamReservation> page = reservationRepository.findAll(
                ExamReservationSpecifications.byFilters(
                        null,
                        null,
                        null,
                        BASE_DATE_TIME.plusDays(1),
                        BASE_DATE_TIME.plusDays(5)
                ),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).extracting(ExamReservation::getStatus)
                .containsExactlyInAnyOrder(ReservationStatus.CANCELLED, ReservationStatus.COMPLETED);
    }

    @Test
    void databaseRejectsSecondActiveReservationForSameStudent() {
        ExamReservation secondActiveReservation = reservation(
                ayan,
                ExamType.PRACTICE,
                BASE_DATE_TIME.plusDays(10),
                ReservationStatus.ACTIVE
        );

        assertThatThrownBy(() -> reservationRepository.saveAndFlush(secondActiveReservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Student student(String iin, String firstName, String lastName, String phone) {
        Student student = new Student();
        student.setIin(iin);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setPhone(phone);
        return student;
    }

    private ExamReservation reservation(
            Student student,
            ExamType examType,
            LocalDateTime examDateTime,
            ReservationStatus status
    ) {
        ExamReservation reservation = new ExamReservation();
        reservation.setStudent(student);
        reservation.setExamType(examType);
        reservation.setExamDateTime(examDateTime);
        reservation.setStatus(status);
        return reservation;
    }
}
