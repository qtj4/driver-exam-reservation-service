package kz.alash.examreservation.repository.spec;

import java.util.Locale;

import kz.alash.examreservation.entity.Student;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class StudentSpecifications {

    private StudentSpecifications() {
    }

    public static Specification<Student> byFilters(String iin, String firstName, String lastName, String phone, String search) {
        return Specification.allOf(
                iinEquals(iin),
                containsIgnoreCase("firstName", firstName),
                containsIgnoreCase("lastName", lastName),
                containsIgnoreCase("phone", phone),
                search(search)
        );
    }

    private static Specification<Student> iinEquals(String iin) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(iin)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("iin"), iin);
        };
    }

    private static Specification<Student> containsIgnoreCase(String field, String value) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(value)) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + value.toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), pattern);
        };
    }

    private static Specification<Student> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("iin")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern)
            );
        };
    }
}
