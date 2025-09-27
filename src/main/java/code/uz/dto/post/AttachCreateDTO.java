package code.uz.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachCreateDTO {
    @NotBlank(message = "ID required")
    private String id;
}
