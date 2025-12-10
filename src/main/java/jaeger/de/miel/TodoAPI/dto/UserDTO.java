package jaeger.de.miel.TodoAPI.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String name;
}
