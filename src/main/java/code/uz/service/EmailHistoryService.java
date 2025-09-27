package code.uz.service;

import code.uz.entity.EmailHistoryEntity;
import code.uz.enums.AppLanguage;
import code.uz.enums.EmailType;
import code.uz.exception.BadException;
import code.uz.repository.EmailHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailHistoryService {
    private final EmailHistoryRepository emailHistoryRepository;
    private final ResourceBundleService bundleService;

    public void create(String email, String code, EmailType type) {
        EmailHistoryEntity emailHistoryEntity = new EmailHistoryEntity();
        emailHistoryEntity.setEmail(email);
        emailHistoryEntity.setCode(code);
        emailHistoryEntity.setEmailType(type);
        emailHistoryEntity.setSendTime(LocalDateTime.now());
        emailHistoryRepository.save(emailHistoryEntity);
    }

    public Long getEmailCountWhileSending(String email) {
        return emailHistoryRepository.countByEmailAndSendTimeBetween(email, LocalDateTime.now().minusMinutes(1), LocalDateTime.now());
    }

    public void checkCode(String email, String code, AppLanguage language) {
        //get last sms by phone
        Optional<EmailHistoryEntity> emailHistoryEntity = emailHistoryRepository.findTop1ByEmailOrderBySendTimeDesc(email);
        if (emailHistoryEntity.isEmpty()) {
            throw new BadException(bundleService.getMessage("verification.failed", language));
        }
        EmailHistoryEntity emailEntity = emailHistoryEntity.get();
        // attempt code check
        if (emailEntity.getAttemptCount() > 3) {
            throw new BadException(bundleService.getMessage("attempt.failed", language));
        }
        // check code
        if (!code.equals(emailEntity.getCode())) {
            emailHistoryRepository.updateAttemptCountByEmail(emailEntity.getEmail()); // +1 attempt qushiladi
            throw new BadException(bundleService.getMessage("verification.code.fail", language));
        }
        // check time
        /// phone_number ga msg boradi code bilan va shu code masalan 2 minut valid buladi tasdiqlash uchun agar 2 minutdan utib ketsa xatolik tashlaydi
        LocalDateTime expDate = emailEntity.getSendTime().plusMinutes(2);
        if (LocalDateTime.now().isAfter(expDate)) {
            throw new BadException(bundleService.getMessage("verification.time.expired", language));
        }
    }
}
