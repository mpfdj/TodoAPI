package jaeger.de.miel.TodoAPI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateListRequestDTO {

    @NotBlank
    @NotNull
    @Size(max = 200)
    private String name;

    @NotBlank
    @NotNull
    @Size(max = 512)
    private String description;
}
