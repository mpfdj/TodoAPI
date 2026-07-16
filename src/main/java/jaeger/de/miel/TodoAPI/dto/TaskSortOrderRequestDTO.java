package jaeger.de.miel.TodoAPI.dto;

import lombok.Data;

@Data
public class TaskSortOrderRequestDTO {
    private Long taskId;
    private Integer sortOrder;
}
