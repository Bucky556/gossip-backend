package code.uz.dto;


import code.uz.enums.GeneralStatus;
import code.uz.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponseDTO {
    private UUID id;
    private String name;
    private String username;
    private List<Role> roles;
    private String accessToken;
    private AttachDTO photo;
    private GeneralStatus status;
    private LocalDateTime createdDate;
    private Long postCount; // oxirog'da qushilgan field
}
