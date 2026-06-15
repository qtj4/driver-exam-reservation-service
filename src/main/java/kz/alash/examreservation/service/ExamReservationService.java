package kz.alash.examreservation.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
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
import kz.alash.examreservation.repository.spec.ExamReservationSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamReservationService {

    private final ExamReservationRepository reservationRepository;
    private final StudentRepository studentRepository;
    private final ExamReservationMapper reservationMapper;
    private final MessageSource messageSource;
    private final Clock clock;

    @Transactional
    public ExamReservationResponse create(ExamReservationCreateRequest request) {
        Student student = findStudent(request.getStudentId());
        validateFutureDateTime(request.getExamDateTime());
        validateStudentHasNoActiveReservation(student.getId());

        ExamReservation reservation = reservationMapper.toEntity(request);
        reservation.setStudent(student);
        reservation.setStatus(ReservationStatus.ACTIVE);

        return reservationMapper.toDto(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ExamReservationResponse getById(UUID id) {
        return reservationMapper.toDto(findReservationWithStudent(id));
    }

    @Transactional(readOnly = true)
    public Page<ExamReservationResponse> getAll(
            UUID studentId,
            ExamType examType,
            ReservationStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        return reservationRepository.findAll(
                        ExamReservationSpecifications.byFilters(studentId, examType, status, from, to),
                        pageable
                )
                .map(reservationMapper::toDto);
    }

    @Transactional
    public ExamReservationResponse update(UUID id, ExamReservationUpdateRequest request) {
        ExamReservation reservation = findReservationWithStudent(id);
        Student targetStudent = resolveTargetStudent(request, reservation);

        if (request.getExamDateTime() != null) {
            validateFutureDateTime(request.getExamDateTime());
        }

        ReservationStatus targetStatus = request.getStatus() == null ? reservation.getStatus() : request.getStatus();
        if (targetStatus == ReservationStatus.ACTIVE) {
            validateStudentHasNoOtherActiveReservation(targetStudent.getId(), id);
        }

        reservationMapper.updateEntity(request, reservation);
        reservation.setStudent(targetStudent);
        return reservationMapper.toDto(reservation);
    }

    @Transactional
    public ExamReservationResponse cancel(UUID id) {
        ExamReservation reservation = findReservationWithStudent(id);

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException(message("reservation.completed.cannot-cancel"));
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toDto(reservation);
    }

    @Transactional
    public void delete(UUID id) {
        ExamReservation reservation = findReservationWithStudent(id);
        reservationRepository.delete(reservation);
        log.debug("Exam reservation {} deleted", id);
    }

    private Student resolveTargetStudent(ExamReservationUpdateRequest request, ExamReservation reservation) {
        if (request.getStudentId() == null || request.getStudentId().equals(reservation.getStudent().getId())) {
            return reservation.getStudent();
        }
        return findStudent(request.getStudentId());
    }

    private Student findStudent(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(message("student.not-found")));
    }

    private ExamReservation findReservationWithStudent(UUID id) {
        return reservationRepository.findByIdWithStudent(id)
                .orElseThrow(() -> new NotFoundException(message("reservation.not-found")));
    }

    private void validateFutureDateTime(LocalDateTime examDateTime) {
        if (!examDateTime.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException(message("reservation.date-time.past"));
        }
    }

    private void validateStudentHasNoActiveReservation(UUID studentId) {
        if (reservationRepository.existsByStudent_IdAndStatus(studentId, ReservationStatus.ACTIVE)) {
            throw new BusinessException(message("reservation.active.already-exists"));
        }
    }

    private void validateStudentHasNoOtherActiveReservation(UUID studentId, UUID reservationId) {
        if (reservationRepository.existsByStudent_IdAndStatusAndIdNot(studentId, ReservationStatus.ACTIVE, reservationId)) {
            throw new BusinessException(message("reservation.active.already-exists"));
        }
    }

    private String message(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, code, locale);
    }
}
