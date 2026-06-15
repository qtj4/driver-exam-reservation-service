package kz.alash.examreservation.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.alash.examreservation.repository.ExamReservationRepository;
import kz.alash.examreservation.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ExamReservationRestIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamReservationRepository reservationRepository;

    @BeforeEach
    void cleanDatabase() {
        reservationRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void createsStudentAndReservationThenCancelsReservationThroughRestApi() throws Exception {
        UUID studentId = createStudent("910101123456");
        LocalDateTime examDateTime = futureDateTime(3);

        MvcResult reservationResult = mockMvc.perform(post("/api/v1/exam-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "studentId", studentId,
                                "examType", "THEORY",
                                "examDateTime", examDateTime
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(studentId.toString()))
                .andExpect(jsonPath("$.examType").value("THEORY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        UUID reservationId = UUID.fromString(jsonNode(reservationResult).get("id").asText());

        mockMvc.perform(get("/api/v1/exam-reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.studentId").value(studentId.toString()));

        mockMvc.perform(patch("/api/v1/exam-reservations/{id}/cancel", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void rejectsSecondActiveReservationForSameStudentThroughRestApi() throws Exception {
        UUID studentId = createStudent("910101123457");

        createReservation(studentId, "THEORY", futureDateTime(4));

        mockMvc.perform(post("/api/v1/exam-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "studentId", studentId,
                                "examType", "PRACTICE",
                                "examDateTime", futureDateTime(5)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void rejectsPastReservationDateTimeThroughRestApiValidation() throws Exception {
        UUID studentId = createStudent("910101123458");

        mockMvc.perform(post("/api/v1/exam-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "studentId", studentId,
                                "examType", "THEORY",
                                "examDateTime", LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.examDateTime").exists());
    }

    @Test
    void deletesStudentAndCascadesReservationsThroughDatabaseRelation() throws Exception {
        UUID studentId = createStudent("910101123459");
        UUID reservationId = createReservation(studentId, "THEORY", futureDateTime(6));

        mockMvc.perform(delete("/api/v1/students/{id}", studentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/students/{id}", studentId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/exam-reservations/{id}", reservationId))
                .andExpect(status().isNotFound());
    }

    private UUID createStudent(String iin) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "iin", iin,
                                "firstName", "Ayan",
                                "lastName", "Karimov",
                                "phone", "+77770000001"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iin").value(iin))
                .andReturn();

        return UUID.fromString(jsonNode(result).get("id").asText());
    }

    private UUID createReservation(UUID studentId, String examType, LocalDateTime examDateTime) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/exam-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "studentId", studentId,
                                "examType", examType,
                                "examDateTime", examDateTime
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        return UUID.fromString(jsonNode(result).get("id").asText());
    }

    private LocalDateTime futureDateTime(int plusDays) {
        return LocalDateTime.now().plusDays(plusDays).truncatedTo(ChronoUnit.SECONDS);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode jsonNode(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
