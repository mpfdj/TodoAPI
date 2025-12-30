package jaeger.de.miel.TodoAPI.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequestDTO {

    @Size(max = 200)
    private String title;

    @Size(max = 512)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Min(value = 1)
    @Max(value = 5)
    private Integer priority;
}
