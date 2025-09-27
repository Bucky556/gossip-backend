package code.uz.repository;

import code.uz.entity.AttachEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AttachRepository extends CrudRepository<AttachEntity, String> {
    @Modifying
    @Query("update AttachEntity set visible = false where id = :id")
    void changeVisibleById(String id);
}
