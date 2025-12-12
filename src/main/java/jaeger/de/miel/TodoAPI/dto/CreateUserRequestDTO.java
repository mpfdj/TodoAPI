package jaeger.de.miel.TodoAPI.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequestDTO {

    @NotBlank
    @Email
    @Size(min = 1, max = 100)
    private String email;

    @NotBlank
    @Size(min = 1, max = 200)
    private String name;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

}
