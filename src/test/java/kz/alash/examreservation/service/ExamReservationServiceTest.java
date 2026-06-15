package kz.alash.examreservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import kz.alash.examreservation.dto.request.ExamReservationCreateRequest;
import kz.alash.examreservation.dto.request.ExamReservationUpdateRequest;
import kz.alash.examreservation.dto.response.ExamReservationResponse;
import kz.alash.examreservation.entity.ExamReservation;
import kz.alash.examreservation.entity.Student;
import kz.alash.examreservation.entity.enums.ExamType;
import kz.alash.examreservation.entity.enums.ReservationStatus;
import kz.alash.examreservation.exception.BusinessException;
import kz.alash.examreservation.exception.NotFoundException;
import kz.alash.examreservation.mapper.ExamReservationMapper;
import kz.alash.examreservation.repository.ExamReservationRepository;
import kz.alash.examreservation.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class ExamReservationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime NOW = LocalDateTime.ofInstant(CLOCK.instant(), CLOCK.getZone());

    @Mock
    private ExamReservationRepository reservationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ExamReservationMapper reservationMapper;

    @Mock
    private MessageSource messageSource;

    private ExamReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ExamReservationService(
                reservationRepository,
                studentRepository,
                reservationMapper,
                messageSource,
                CLOCK
        );
        lenient().when(messageSource.getMessage(anyString(), isNull(), anyString(), any(Locale.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createReservationSuccessfully() {
        Student student = student();
        ExamReservationCreateRequest request = createRequest(student.getId(), NOW.plusDays(1));
        ExamReservation reservation = new ExamReservation();
        ExamReservationResponse response = ExamReservationResponse.builder().status(ReservationStatus.ACTIVE).build();

        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(reservationRepository.existsByStudent_IdAndStatus(student.getId(), ReservationStatus.ACTIVE)).thenReturn(false);
        when(reservationMapper.toEntity(request)).thenReturn(reservation);
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(reservationMapper.toDto(reservation)).thenReturn(response);

        ExamReservationResponse result = reservationService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(reservation.getStudent()).isEqualTo(student);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void createFailsWhenStudentDoesNotExist() {
        UUID studentId = UUID.randomUUID();
        ExamReservationCreateRequest request = createRequest(studentId, NOW.plusDays(1));
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("student.not-found");
    }

    @Test
    void createFailsWhenDateTimeIsInPast() {
        Student student = student();
        ExamReservationCreateRequest request = createRequest(student.getId(), NOW.minusMinutes(1));
        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("reservation.date-time.past");
    }

    @Test
    void createFailsWhenStudentAlreadyHasActiveReservation() {
        Student student = student();
        ExamReservationCreateRequest request = createRequest(student.getId(), NOW.plusDays(1));
        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(reservationRepository.existsByStudent_IdAndStatus(student.getId(), ReservationStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("reservation.active.already-exists");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createSetsStatusToActive() {
        Student student = student();
        ExamReservationCreateRequest request = createRequest(student.getId(), NOW.plusDays(1));
        ExamReservation reservation = new ExamReservation();

        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(reservationRepository.existsByStudent_IdAndStatus(student.getId(), ReservationStatus.ACTIVE)).thenReturn(false);
        when(reservationMapper.toEntity(request)).thenReturn(reservation);
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(reservationMapper.toDto(reservation)).thenReturn(ExamReservationResponse.builder().build());

        reservationService.create(request);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void cancelActiveReservationSuccessfully() {
        UUID reservationId = UUID.randomUUID();
        ExamReservation reservation = reservation(ReservationStatus.ACTIVE);
        ExamReservationResponse response = ExamReservationResponse.builder().status(ReservationStatus.CANCELLED).build();

        when(reservationRepository.findByIdWithStudent(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toDto(reservation)).thenReturn(response);

        ExamReservationResponse result = reservationService.cancel(reservationId);

        assertThat(result).isEqualTo(response);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancelCompletedReservationFails() {
        UUID reservationId = UUID.randomUUID();
        ExamReservation reservation = reservation(ReservationStatus.COMPLETED);
        when(reservationRepository.findByIdWithStudent(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(reservationId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("reservation.completed.cannot-cancel");

        verify(reservationMapper, never()).toDto(any());
    }

    @Test
    void updateToActiveFailsIfAnotherActiveReservationExistsForSameStudent() {
        UUID reservationId = UUID.randomUUID();
        ExamReservation reservation = reservation(ReservationStatus.CANCELLED);
        ExamReservationUpdateRequest request = new ExamReservationUpdateRequest();
        request.setStatus(ReservationStatus.ACTIVE);

        when(reservationRepository.findByIdWithStudent(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.existsByStudent_IdAndStatusAndIdNot(
                reservation.getStudent().getId(),
                ReservationStatus.ACTIVE,
                reservationId
        )).thenReturn(true);

        assertThatThrownBy(() -> reservationService.update(reservationId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("reservation.active.already-exists");
    }

    @Test
    void getByIdFailsWhenReservationNotFound() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findByIdWithStudent(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getById(reservationId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("reservation.not-found");
    }

    private ExamReservationCreateRequest createRequest(UUID studentId, LocalDateTime examDateTime) {
        ExamReservationCreateRequest request = new ExamReservationCreateRequest();
        request.setStudentId(studentId);
        request.setExamType(ExamType.THEORY);
        request.setExamDateTime(examDateTime);
        return request;
    }

    private Student student() {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        student.setIin("123456789012");
        student.setFirstName("Ayan");
        student.setLastName("Karimov");
        student.setPhone("+77770000001");
        return student;
    }

    private ExamReservation reservation(ReservationStatus status) {
        ExamReservation reservation = new ExamReservation();
        reservation.setStudent(student());
        reservation.setExamType(ExamType.THEORY);
        reservation.setExamDateTime(NOW.plusDays(1));
        reservation.setStatus(status);
        return reservation;
    }
}
