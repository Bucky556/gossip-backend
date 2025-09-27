package code.uz.dto.profile;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileNameDTO {
    @NotBlank(message = "Name required")
    private String name;
}
