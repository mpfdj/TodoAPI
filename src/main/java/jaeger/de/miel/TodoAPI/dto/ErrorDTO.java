package jaeger.de.miel.TodoAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ErrorDTO {
    private String errorMessage;
}
