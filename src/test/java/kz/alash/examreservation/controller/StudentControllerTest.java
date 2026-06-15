package kz.alash.examreservation.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kz.alash.examreservation.config.MessageResourceConfig;
import kz.alash.examreservation.handler.GlobalExceptionHandler;
import kz.alash.examreservation.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentController.class)
@Import({GlobalExceptionHandler.class, MessageResourceConfig.class})
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @Test
    void invalidIinReturnsFieldError() throws Exception {
        String body = """
                {
                  "iin": "12345",
                  "firstName": "Ayan",
                  "lastName": "Karimov",
                  "phone": "+77770000001"
                }
                """;

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "en")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.iin", notNullValue()));
    }

    @Test
    void missingRequiredFieldReturnsFieldError() throws Exception {
        String body = """
                {
                  "iin": "123456789012",
                  "lastName": "Karimov",
                  "phone": "+77770000001"
                }
                """;

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "en")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.firstName", notNullValue()));
    }

    @Test
    void invalidPageReturnsValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/students").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.page", notNullValue()));
    }
}
