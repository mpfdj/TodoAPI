package jaeger.de.miel.TodoAPI.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskRequestDTO {
//    private Long listId;
//    private Long userId ;

    @NotNull
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    @NotBlank
    @Size(max = 512)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @NotNull
    @FutureOrPresent
    @JsonFormat
    private LocalDate dueDate;

    @NotNull
    @Min(value = 1)
    @Max(value = 5)
    private Integer priority;
}
