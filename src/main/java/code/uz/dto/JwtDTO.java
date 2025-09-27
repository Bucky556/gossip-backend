package code.uz.dto;

import code.uz.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class JwtDTO {
    private String username;
    private UUID id;
    private List<Role> roles;
}
