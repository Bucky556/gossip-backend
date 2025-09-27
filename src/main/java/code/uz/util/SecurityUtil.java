package code.uz.util;

import code.uz.config.CustomUserDetails;
import code.uz.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.UUID;

public class SecurityUtil {

    public static UUID getID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getId();
    }

    public static boolean hasRole(Role role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream().anyMatch(simple -> simple.getAuthority().equals(role.name()));
    }
}
