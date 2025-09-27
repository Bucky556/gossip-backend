package code.uz.dto.profile;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilePasswordDTO {
    @NotBlank(message = "Current password required")
    private String currentPassword;
    @NotBlank(message = "New password required")
    private String newPassword;
}
