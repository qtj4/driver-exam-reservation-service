package kz.alash.examreservation.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "student",
        indexes = {
                @Index(name = "student_iin_index", columnList = "iin", unique = true),
                @Index(name = "student_last_name_index", columnList = "last_name")
        }
)
@Getter
@Setter
public class Student extends BaseEntity {

    @Column(name = "iin", nullable = false, length = 12, unique = true)
    private String iin;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ExamReservation> reservations = new ArrayList<>();
}
