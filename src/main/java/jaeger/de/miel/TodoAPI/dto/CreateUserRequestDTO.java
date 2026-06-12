package jaeger.de.miel.TodoAPI.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {

    private transient PasswordEncoder passwordEncoder;

    @NotBlank
    @NotNull
    @Email
    @Size(max = 128)
    private String email;

    @NotBlank
    @NotNull
    @Size(max = 200)
    private String name;

    @NotBlank
    @NotNull
    @Size(max = 128)
    private String password;

    private void setPassword(String password) {
        this.password = passwordEncoder.encode(password);
    }

}
