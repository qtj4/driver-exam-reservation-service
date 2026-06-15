package kz.alash.examreservation.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String error;

    private int status;

    private String message;

    private Map<String, String> fieldErrors;

    private LocalDateTime timestamp;
}
