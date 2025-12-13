package jaeger.de.miel.TodoAPI.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequestDTO {

    @NotBlank
    @NotNull
    @Email
    @Size(max = 128)
    private String email;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @NotNull
    @Size(max = 128)
    private String password;

}
