package code.uz.dto.profile;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUsernameDTO {
    @NotBlank(message = "Email or phone required")
    private String username;
}
