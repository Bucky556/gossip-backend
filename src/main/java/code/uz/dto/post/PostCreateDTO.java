package code.uz.dto.post;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateDTO {
    @NotBlank(message = "Title required")
    private String title;
    @NotBlank(message = "Content required")
    private String content;
    @Valid
    private AttachCreateDTO photo;
}
