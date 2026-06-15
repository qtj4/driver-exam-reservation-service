package kz.alash.examreservation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private UUID id;

    private String iin;

    private String firstName;

    private String lastName;

    private String phone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
