package code.uz.service;

import code.uz.entity.SmsHistoryEntity;
import code.uz.enums.AppLanguage;
import code.uz.enums.SmsType;
import code.uz.exception.BadException;
import code.uz.repository.SmsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SmsHistoryService {
    private final SmsHistoryRepository smsHistoryRepository;
    private final ResourceBundleService bundleService;

    public void create(String phoneNumber, String message, String code, SmsType type) {
        SmsHistoryEntity smsHistoryEntity = new SmsHistoryEntity();
        smsHistoryEntity.setPhone(phoneNumber);
        smsHistoryEntity.setMessage(message);
        smsHistoryEntity.setCode(code);
        smsHistoryEntity.setSmsType(type);
        smsHistoryEntity.setSendTime(LocalDateTime.now());
        smsHistoryRepository.save(smsHistoryEntity);
    }

    public Long getSmsCountWhileSending(String phoneNumber) {
        return smsHistoryRepository.countByPhoneAndSendTimeBetween(phoneNumber, LocalDateTime.now().minusMinutes(1), LocalDateTime.now());
    }

    public void checkCode(String phoneNumber, String code, AppLanguage language) {
        //get last sms by phone
        Optional<SmsHistoryEntity> smsHistoryEntity = smsHistoryRepository.findTop1ByPhoneOrderBySendTimeDesc(phoneNumber);
        if (smsHistoryEntity.isEmpty()) {
            throw new BadException(bundleService.getMessage("verification.failed", language));
        }
        SmsHistoryEntity smsEntity = smsHistoryEntity.get();
        // attempt code check
        if (smsEntity.getAttemptCount() > 3) {
            throw new BadException(bundleService.getMessage("attempt.failed", language));
        }
        // check code
        if (!code.equals(smsEntity.getCode())) {
            smsHistoryRepository.updateAttemptCountByPhone(smsEntity.getPhone()); // +1 attempt qushiladi
            throw new BadException(bundleService.getMessage("verification.code.fail", language));
        }
        // check time
        /// phone_number ga msg boradi code bilan va shu code masalan 2 minut valid buladi tasdiqlash uchun agar 2 minutdan utib ketsa xatolik tashlaydi
        LocalDateTime expDate = smsEntity.getSendTime().plusMinutes(2);
        if (LocalDateTime.now().isAfter(expDate)) {
            throw new BadException(bundleService.getMessage("verification.time.expired", language));
        }
    }
}
