package kz.alash.examreservation.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kz.alash.examreservation.config.MessageResourceConfig;
import kz.alash.examreservation.handler.GlobalExceptionHandler;
import kz.alash.examreservation.service.ExamReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExamReservationController.class)
@Import({GlobalExceptionHandler.class, MessageResourceConfig.class})
class ExamReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExamReservationService reservationService;

    @Test
    void missingStudentIdReturnsFieldError() throws Exception {
        String body = """
                {
                  "examType": "THEORY",
                  "examDateTime": "2026-06-16T09:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/exam-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "en")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.studentId", notNullValue()));
    }

    @Test
    void invalidPageSizeReturnsValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/exam-reservations").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.size", notNullValue()));
    }
}
