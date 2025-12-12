package jaeger.de.miel.TodoAPI.dto;

import lombok.Data;

@Data
public class ListDTO {
    private Long id;
    private Long userId;
    private String name;
    private String description;
}
