package code.uz.dto.auth;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {
    @NotBlank(message = "Email or Phone number required")
    private String username;
}
