package code.uz.repository;

import code.uz.entity.ProfileEntity;
import code.uz.enums.GeneralStatus;
import code.uz.mapper.ProfileMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Transactional
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {

    Optional<ProfileEntity> findByUsernameAndVisibleTrue(String emailOrPhone);

    Optional<ProfileEntity> findByIdAndVisibleTrue(UUID id);

    @Query("update ProfileEntity p set p.status = :status where p.id = :id")
    @Modifying
    void updateStatusById(GeneralStatus status, UUID id);

    @Query("update ProfileEntity p set p.password = :confirmPassword where p.id = :id")
    @Modifying
    void updatePasswordById(UUID id, String confirmPassword);

    @Query("update ProfileEntity p set p.name = :name where p.id = :id")
    @Modifying
    void updateNameById(UUID id, String name);

    @Query("update ProfileEntity p set p.updatedUsername = :upd_username where p.id = :id")
    @Modifying
    void updateUpd_UsernameById(UUID id, String upd_username);

    @Query("update ProfileEntity p set p.username = :username where p.id = :id")
    @Modifying
    void updateUsernameById(UUID id, String username);

    @Query("update ProfileEntity set photoId = :photoId where id = :id")
    @Modifying
    void updatePhoto(UUID id, String photoId);

    /// inner join fetch performence-ga yaxshi, ya'ni query jamroq ketadi
    /*@Query("from ProfileEntity as p inner join fetch p.role where p.visible = true order by p.createdDate desc")
    Page<ProfileEntity> findAllByVisibleTrueOrderByCreatedDateDesc(PageRequest pageRequest);*/

    @Query(value = "select p.id as id, p.username as username, p.name as name, p.created_date as createdDate, p.status as status, p.photo_id as photoId, " +
            "(select count (post) from post as post where post.profile_id = p.id ) as postCount, " +
            "(select string_agg(p_role, ',') from profile_role as pr where pr.profile_id = p.id) as roleList " +
            "from profile as p " +
            "where (lower(p.username) like ?1 or lower(p.name) like ?1 ) and p.visible = true order by p.created_date desc ", nativeQuery = true,
            countQuery = "select count(*) from profile as p where (lower(p.username) like ?1 or lower(p.name) like ?1 ) and visible = true ") // count nechtaligini bilish uchun yozib quyildi
    Page<ProfileMapper> filterProfileWithPostCount(String query, PageRequest pageRequest);

    @Query(value = "select p.id as id, p.username as username, p.name as name, p.created_date as createdDate, p.status as status, p.photo_id as photoId, " +
            "(select count (post) from post as post where post.profile_id = p.id ) as postCount, " +
            "(select string_agg(p_role, ',') from profile_role as pr where pr.profile_id = p.id) as roleList " +
            "from profile as p " +
            "where p.visible = true order by p.created_date desc ", nativeQuery = true,
            countQuery = "select count(*) from profile where visible = true ") // count nechtaligini bilish uchun yozib quyildi
    Page<ProfileMapper> filterAllWithPostCount(PageRequest pageRequest);

    @Modifying
    @Query("update ProfileEntity set visible = false where id = :id")
    void changeVisibleById(UUID id);

}
