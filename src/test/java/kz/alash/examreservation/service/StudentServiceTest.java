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

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import kz.alash.examreservation.dto.request.StudentCreateRequest;
import kz.alash.examreservation.dto.request.StudentUpdateRequest;
import kz.alash.examreservation.dto.response.StudentResponse;
import kz.alash.examreservation.entity.Student;
import kz.alash.examreservation.exception.BusinessException;
import kz.alash.examreservation.exception.NotFoundException;
import kz.alash.examreservation.mapper.StudentMapper;
import kz.alash.examreservation.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private MessageSource messageSource;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository, studentMapper, messageSource);
        lenient().when(messageSource.getMessage(anyString(), isNull(), anyString(), any(Locale.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createCreatesStudentSuccessfully() {
        StudentCreateRequest request = createRequest();
        Student entity = student(request.getIin());
        Student saved = student(request.getIin());
        UUID studentId = UUID.randomUUID();
        saved.setId(studentId);
        StudentResponse response = StudentResponse.builder().id(studentId).iin(request.getIin()).build();

        when(studentRepository.existsByIin(request.getIin())).thenReturn(false);
        when(studentMapper.toEntity(request)).thenReturn(entity);
        when(studentRepository.save(entity)).thenReturn(saved);
        when(studentMapper.toDto(saved)).thenReturn(response);

        StudentResponse result = studentService.create(request);

        assertThat(result).isEqualTo(response);
        verify(studentRepository).save(entity);
    }

    @Test
    void createFailsWhenIinAlreadyExists() {
        StudentCreateRequest request = createRequest();
        when(studentRepository.existsByIin(request.getIin())).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("student.iin.already-exists");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateFailsWhenIinBelongsToAnotherStudent() {
        UUID studentId = UUID.randomUUID();
        StudentUpdateRequest request = updateRequest();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student("123456789012")));
        when(studentRepository.existsByIinAndIdNot(request.getIin(), studentId)).thenReturn(true);

        assertThatThrownBy(() -> studentService.update(studentId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("student.iin.already-exists");

        verify(studentMapper, never()).updateEntity(any(), any());
    }

    @Test
    void getByIdFailsWhenStudentNotFound() {
        UUID studentId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getById(studentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("student.not-found");
    }

    private StudentCreateRequest createRequest() {
        StudentCreateRequest request = new StudentCreateRequest();
        request.setIin("123456789012");
        request.setFirstName("Ayan");
        request.setLastName("Karimov");
        request.setPhone("+77770000001");
        return request;
    }

    private StudentUpdateRequest updateRequest() {
        StudentUpdateRequest request = new StudentUpdateRequest();
        request.setIin("123456789013");
        request.setFirstName("Ayan");
        request.setLastName("Karimov");
        request.setPhone("+77770000002");
        return request;
    }

    private Student student(String iin) {
        Student student = new Student();
        student.setIin(iin);
        student.setFirstName("Ayan");
        student.setLastName("Karimov");
        student.setPhone("+77770000001");
        return student;
    }
}
