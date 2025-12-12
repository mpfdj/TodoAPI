package jaeger.de.miel.TodoAPI.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class TaskDTO {
    private Long id;
    private Long listId;
    private Long userId;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private Integer priority;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
}
