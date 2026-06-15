package kz.alash.examreservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StudentCreateRequest {

    @NotBlank(message = "{student.iin.required}")
    @Pattern(regexp = "\\d{12}", message = "{student.iin.invalid}")
    private String iin;

    @NotBlank(message = "{student.first-name.required}")
    private String firstName;

    @NotBlank(message = "{student.last-name.required}")
    private String lastName;

    @NotBlank(message = "{student.phone.required}")
    private String phone;
}
