package code.uz.config;

import code.uz.entity.ProfileEntity;
import code.uz.entity.ProfileRoleEntity;
import code.uz.repository.ProfileRepository;
import code.uz.repository.ProfileRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final ProfileRepository profileRepository;
    private final ProfileRoleRepository profileRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ProfileEntity> profile = profileRepository.findByUsernameAndVisibleTrue(username);
        if (profile.isEmpty()) {
            throw new UsernameNotFoundException("Username not found");
        }
        ProfileEntity profileEntity = profile.get();
        List<ProfileRoleEntity> roleList = profileRoleRepository.findAllByProfileId(profileEntity.getId());

        return new CustomUserDetails(profileEntity, roleList);
    }
}
