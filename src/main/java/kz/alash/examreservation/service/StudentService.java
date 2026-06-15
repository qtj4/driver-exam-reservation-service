package kz.alash.examreservation.service;

import java.util.Locale;
import java.util.UUID;

import kz.alash.examreservation.dto.request.StudentCreateRequest;
import kz.alash.examreservation.dto.request.StudentUpdateRequest;
import kz.alash.examreservation.dto.response.StudentResponse;
import kz.alash.examreservation.entity.Student;
import kz.alash.examreservation.exception.BusinessException;
import kz.alash.examreservation.exception.NotFoundException;
import kz.alash.examreservation.mapper.StudentMapper;
import kz.alash.examreservation.repository.StudentRepository;
import kz.alash.examreservation.repository.spec.StudentSpecifications;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final MessageSource messageSource;

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        if (studentRepository.existsByIin(request.getIin())) {
            throw new BusinessException(message("student.iin.already-exists"));
        }

        Student student = studentMapper.toEntity(request);
        return studentMapper.toDto(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(UUID id) {
        return studentMapper.toDto(findStudent(id));
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getAll(
            String iin,
            String firstName,
            String lastName,
            String phone,
            String search,
            Pageable pageable
    ) {
        return studentRepository.findAll(StudentSpecifications.byFilters(iin, firstName, lastName, phone, search), pageable)
                .map(studentMapper::toDto);
    }

    @Transactional
    public StudentResponse update(UUID id, StudentUpdateRequest request) {
        Student student = findStudent(id);

        if (studentRepository.existsByIinAndIdNot(request.getIin(), id)) {
            throw new BusinessException(message("student.iin.already-exists"));
        }

        studentMapper.updateEntity(request, student);
        return studentMapper.toDto(student);
    }

    @Transactional
    public void delete(UUID id) {
        Student student = findStudent(id);
        studentRepository.delete(student);
        log.debug("Student {} deleted", id);
    }

    private Student findStudent(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(message("student.not-found")));
    }

    private String message(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, code, locale);
    }
}
