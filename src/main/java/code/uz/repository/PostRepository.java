package code.uz.repository;

import code.uz.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
public interface PostRepository extends JpaRepository<PostEntity, String> {

    Page<PostEntity> findAllByProfileIdAndVisibleTrueOrderByCreatedDateDesc(UUID profileId, Pageable pageable);

    @Modifying
    @Query("update PostEntity set visible = false where id = :id")
    void changeVisibleById(String id);
}
