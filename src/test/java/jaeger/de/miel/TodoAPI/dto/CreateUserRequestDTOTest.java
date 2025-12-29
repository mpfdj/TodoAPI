package jaeger.de.miel.TodoAPI.dto;

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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateUserRequestDTOTest {

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
                  "email": "unittest@mail.com",
                  "name": "name",
                  "password": "password"
                }
                """;

        // when
        CreateUserRequestDTO dto = objectMapper.readValue(json, CreateUserRequestDTO.class);
        Set<ConstraintViolation<CreateUserRequestDTO>> violations = validator.validate(dto);

        // then
        assertTrue(violations.isEmpty(), () -> "Expected no violations, got: " + violations);

        // field assertions
        assertEquals("unittest@mail.com", dto.getEmail());
        assertEquals("name", dto.getName());
        assertEquals("password", dto.getPassword());
    }

}