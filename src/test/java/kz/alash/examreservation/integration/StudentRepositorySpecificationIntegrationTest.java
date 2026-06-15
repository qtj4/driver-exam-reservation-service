package kz.alash.examreservation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import kz.alash.examreservation.config.JpaAuditConfig;
import kz.alash.examreservation.entity.Student;
import kz.alash.examreservation.repository.StudentRepository;
import kz.alash.examreservation.repository.spec.StudentSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditConfig.class)
class StudentRepositorySpecificationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        studentRepository.saveAllAndFlush(List.of(
                student("910101111111", "Ayan", "Karimov", "+77770000001"),
                student("920202222222", "Dana", "Omarova", "+77770000002"),
                student("930303333333", "Miras", "Sadykov", "+77771234567")
        ));
    }

    @Test
    void searchFilterFindsStudentsByIinFirstNameLastNameAndPhone() {
        Page<Student> byIin = search("920202");
        Page<Student> byFirstName = search("ayan");
        Page<Student> byLastName = search("SADYKOV");
        Page<Student> byPhone = search("1234567");

        assertThat(byIin.getContent()).extracting(Student::getIin).containsExactly("920202222222");
        assertThat(byFirstName.getContent()).extracting(Student::getIin).containsExactly("910101111111");
        assertThat(byLastName.getContent()).extracting(Student::getIin).containsExactly("930303333333");
        assertThat(byPhone.getContent()).extracting(Student::getIin).containsExactly("930303333333");
    }

    @Test
    void iinFilterReturnsOnlyExactStudent() {
        Page<Student> page = studentRepository.findAll(
                StudentSpecifications.byFilters("910101111111", null, null, null, null),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).extracting(Student::getFirstName).containsExactly("Ayan");
    }

    @Test
    void nameAndPhoneFiltersCanBeCombined() {
        Page<Student> page = studentRepository.findAll(
                StudentSpecifications.byFilters(null, "da", "oma", "+77770000002", null),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).extracting(Student::getIin).containsExactly("920202222222");
    }

    private Page<Student> search(String search) {
        return studentRepository.findAll(
                StudentSpecifications.byFilters(null, null, null, null, search),
                PageRequest.of(0, 10)
        );
    }

    private Student student(String iin, String firstName, String lastName, String phone) {
        Student student = new Student();
        student.setIin(iin);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setPhone(phone);
        return student;
    }
}
