
package jaeger.de.miel.TodoAPI.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateTaskRequestDTOTest {

    private ObjectMapper objectMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);  // fail fast on unknown fields

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldDeserializeAndValidate_ok() throws Exception {
        // given
        String json = """
                {
                  "title": "title",
                  "description": "description",
                  "status": "todo",
                  "dueDate": "2030-12-31",
                  "priority": 5
                }
                """;

        // when
        CreateTaskRequestDTO dto = objectMapper.readValue(json, CreateTaskRequestDTO.class);
        Set<ConstraintViolation<CreateTaskRequestDTO>> violations = validator.validate(dto);

        // then
        assertTrue(violations.isEmpty(), () -> "Expected no violations, got: " + violations);

        // field assertions
        assertEquals("title", dto.getTitle());
        assertEquals("description", dto.getDescription());
        assertEquals(TaskStatus.TODO, dto.getStatus());
        assertEquals(LocalDate.of(2030, 12, 31), dto.getDueDate());
        assertEquals(5, dto.getPriority());
    }

    @Test
    void shouldDeserialize_caseInsensitiveSnakeCaseStatus_inProgress() throws Exception {
        String json = """
                {
                  "title": "Job",
                  "description": "Do something",
                  "status": "in_progress",
                  "dueDate": "2030-01-01",
                  "priority": 3
                }
                """;

        CreateTaskRequestDTO dto = objectMapper.readValue(json, CreateTaskRequestDTO.class);
        Set<ConstraintViolation<CreateTaskRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals(TaskStatus.IN_PROGRESS, dto.getStatus());
    }

    @Test
    void shouldFailDeserialization_onInvalidEnumValue() {
        String json = """
                {
                  "title": "title",
                  "description": "description",
                  "status": "invalid_status",
                  "dueDate": "2030-12-31",
                  "priority": 1
                }
                """;

        // Jackson should throw before Bean Validation runs because enum can't be constructed
        Exception ex = assertThrows(JsonProcessingException.class,
                () -> objectMapper.readValue(json, CreateTaskRequestDTO.class));

        // Optional: check message contains our enum's error
        assertTrue(ex.getMessage().toLowerCase().contains("invalid taskstatus")
                || ex.getMessage().toLowerCase().contains("invalid_status"));
    }

    @Test
    void shouldFailValidation_onMissingRequiredFields() throws Exception {
        // Missing title and description; status okay; dueDate and priority present
        String json = """
                {
                  "status": "todo",
                  "dueDate": "2030-12-31",
                  "priority": 2
                }
                """;

        CreateTaskRequestDTO dto = objectMapper.readValue(json, CreateTaskRequestDTO.class);
        Set<ConstraintViolation<CreateTaskRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        // Expect violations on title and description (both @NotNull and @NotBlank)
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void shouldFailValidation_onPastDueDateOrPriorityOutOfRange() throws Exception {
        String json = """
                {
                  "title": "title",
                  "description": "description",
                  "status": "todo",
                  "dueDate": "2000-01-01",
                  "priority": 7
                }
                """;

        CreateTaskRequestDTO dto = objectMapper.readValue(json, CreateTaskRequestDTO.class);
        Set<ConstraintViolation<CreateTaskRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());

        // dueDate violates @FutureOrPresent
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("dueDate")
                        && v.getMessage().toLowerCase().contains("today or in the future")));

        // priority violates @Max(5)
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("priority")
                        && v.getMessage().toLowerCase().contains("at most 5")));
    }

}