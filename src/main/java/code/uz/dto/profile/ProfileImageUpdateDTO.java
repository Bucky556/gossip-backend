package code.uz.dto.profile;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileImageUpdateDTO {
    @NotBlank(message = "Photo id required")
    private String photoId;
}
