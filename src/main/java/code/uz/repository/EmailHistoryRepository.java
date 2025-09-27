package code.uz.repository;

import code.uz.entity.EmailHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
public interface EmailHistoryRepository extends JpaRepository<EmailHistoryEntity, String> {
    Long countByEmailAndSendTimeBetween(String email, LocalDateTime from, LocalDateTime to);

    Optional<EmailHistoryEntity> findTop1ByEmailOrderBySendTimeDesc(String email);

    @Modifying
    @Query("update EmailHistoryEntity s set s.attemptCount = coalesce(s.attemptCount, 0) + 1 where s.email = :email")
    /// coalesce(arg1, arg2) -> bu method agar birinchi qiymat null bulsa 2 chi qiymatni oladi, toki null dan boshqa son chiqguncha faqat son oladi.
    /// Bu null qiymat olishdan saqlaydi.
    void updateAttemptCountByEmail(String email);
}
