package jaeger.de.miel.TodoAPI.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateListRequestDTO {

    @Size(max = 200)
    private String name;

    @Size(max = 512)
    private String description;

}
