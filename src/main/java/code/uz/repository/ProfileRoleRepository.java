package code.uz.repository;

import code.uz.entity.ProfileRoleEntity;
import code.uz.enums.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
public interface ProfileRoleRepository extends CrudRepository<ProfileRoleEntity, Integer> {
    @Modifying
    void deleteByProfileId(UUID profileId);

    List<ProfileRoleEntity> findAllByProfileId(UUID profileId);
}
