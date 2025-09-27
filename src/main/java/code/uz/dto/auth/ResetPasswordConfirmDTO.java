package code.uz.dto.auth;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordConfirmDTO {
    @NotBlank(message = "Email or Phone required")
    private String username;
    @NotBlank(message = "Confirm Code required")
    private String confirmCode;
    @NotBlank(message = " New Password required")
    private String newPassword;
}
