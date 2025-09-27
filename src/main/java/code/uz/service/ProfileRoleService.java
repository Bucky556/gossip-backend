package code.uz.service;

import code.uz.entity.ProfileRoleEntity;
import code.uz.enums.Role;
import code.uz.repository.ProfileRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileRoleService {
    private final ProfileRoleRepository profileRoleRepository;

    public void create(UUID profileId, List<Role> roles) {
        List<ProfileRoleEntity> roleEntities = roles.stream()
                .map(role -> {
                    ProfileRoleEntity profileRoleEntity = new ProfileRoleEntity();
                    profileRoleEntity.setProfileId(profileId);
                    profileRoleEntity.setRole(role);
                    return profileRoleEntity;
                })
                .toList();
        profileRoleRepository.saveAll(roleEntities);
    }

    public void deleteRoles(UUID profileId) {
        profileRoleRepository.deleteByProfileId(profileId);
    }
}
