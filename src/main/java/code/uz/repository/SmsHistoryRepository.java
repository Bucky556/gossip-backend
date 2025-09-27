package code.uz.repository;

import code.uz.entity.SmsHistoryEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
public interface SmsHistoryRepository extends CrudRepository<SmsHistoryEntity, String> {
    Long countByPhoneAndSendTimeBetween(String phone, LocalDateTime from, LocalDateTime to);

    Optional<SmsHistoryEntity> findTop1ByPhoneOrderBySendTimeDesc(String phone);

    @Modifying
    @Query("update SmsHistoryEntity s set s.attemptCount = coalesce(s.attemptCount, 0) + 1 where s.phone = :phone")
    /// coalesce(arg1, arg2) -> bu method agar birinchi qiymat null bulsa, 2 chi qiymatni oladi, toki null dan boshqa son chiqguncha faqat son oladi.
    /// Bu null qiymat olishdan saqlaydi.
    void updateAttemptCountByPhone(String phone);
}
