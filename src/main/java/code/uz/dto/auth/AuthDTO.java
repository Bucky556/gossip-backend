package code.uz.dto.auth;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthDTO {
    @NotBlank(message = "phone or email required")
    private String username;
    @NotBlank(message = "password required")
    private String password;
}
