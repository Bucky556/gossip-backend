package code.uz.config;

import code.uz.entity.ProfileEntity;
import code.uz.entity.ProfileRoleEntity;
import code.uz.enums.GeneralStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {
    private final UUID id;
    private final String name;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> roles;
    private final GeneralStatus status;
    private final String photoId;

    public CustomUserDetails(ProfileEntity profileEntity, List<ProfileRoleEntity> roles) {
        this.id = profileEntity.getId();
        this.name = profileEntity.getName();
        this.username = profileEntity.getUsername();
        this.password = profileEntity.getPassword();
        this.status = profileEntity.getStatus();
        this.roles = roles.stream().map(role -> new SimpleGrantedAuthority(role.getRole().name())).toList();
        this.photoId = profileEntity.getPhotoId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status.equals(GeneralStatus.ACTIVE);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
