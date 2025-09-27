package code.uz.service;

import code.uz.dto.sms.SmsAuthDTO;
import code.uz.dto.sms.SmsRequestDTO;
import code.uz.dto.sms.SmsSendResponseDTO;
import code.uz.entity.SmsTokenEntity;
import code.uz.enums.AppLanguage;
import code.uz.enums.SmsType;
import code.uz.exception.BadException;
import code.uz.repository.SmsTokenRepository;
import code.uz.util.RandomUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SmsSendService {
    private final RestTemplate restTemplate;
    @Value("${eskiz.url}")
    private String eskizURL;
    @Value("${eskiz.login}")
    private String accountLogin;
    @Value("${eskiz.pswd}")
    private String accountPswd;
    private final SmsTokenRepository smsTokenRepository;
    private final SmsHistoryService smsHistoryService;
    private final ResourceBundleService bundleService;

    public void sendRegistrationSms(String phoneNumber, AppLanguage language) {
        String code = RandomUtil.generateRandomCode();
        String message = bundleService.getMessage("sms.message", language); /// bu yerda code %s ham qushilish kk edi lekn sms test rejimda bulganligi uchun qushib bulmaydi. DB dan qarab quyaveramiz
        message = String.format(message, code);
        sendSms(phoneNumber, message, code, SmsType.REGISTRATION, language);
    }

    public void sendSmsForUpdateUsername(String phoneNumber, AppLanguage language) {
        String code = RandomUtil.generateRandomCode();
        String message = bundleService.getMessage("sms.message", language); /// bu yerda code %s ham qushilish kk edi lekn sms test rejimda bulganligi uchun qushib bulmaydi. DB dan qarab quyaveramiz
        message = String.format(message, code);
        sendSms(phoneNumber, message, code, SmsType.UPDATE_USERNAME, language);
    }

    private SmsSendResponseDTO sendSms(String phoneNumber, String message, String code, SmsType smsType, AppLanguage language) {
        Long smsCount = smsHistoryService.getSmsCountWhileSending(phoneNumber);
        Integer smsLimit = 3;
        if (smsCount > smsLimit) {
            throw new BadException(bundleService.getMessage("sms.limit.exceeded", language));
        }

        SmsSendResponseDTO responseDTO = sendSmsBody(phoneNumber, message);
        smsHistoryService.create(phoneNumber, message, code, smsType);  // bu method db ga saqlash uchun yozildi
        return responseDTO;
    }

    private SmsSendResponseDTO sendSmsBody(String phoneNumber, String message) {
        String token = getTokenExisted();
        // header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        httpHeaders.set("Authorization", "Bearer " + token);

        // body
        SmsRequestDTO body = new SmsRequestDTO();
        body.setMobile_phone(phoneNumber);
        body.setMessage(message);
        body.setFrom("4546");

        HttpEntity<SmsRequestDTO> httpEntity = new HttpEntity<>(body, httpHeaders);
        String url = eskizURL + "/message/sms/send";
        try {
            ResponseEntity<SmsSendResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, SmsSendResponseDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTokenExisted() {
        Optional<SmsTokenEntity> entity = smsTokenRepository.findTopByOrderByIdDesc();
        if (entity.isEmpty()) {
            SmsTokenEntity smsTokenEntity = new SmsTokenEntity();
            smsTokenEntity.setToken(getNewToken());
            smsTokenEntity.setCreatedDate(LocalDateTime.now());
            smsTokenEntity.setExpDate(LocalDateTime.now().plusMonths(1));
            smsTokenRepository.save(smsTokenEntity);
            return getNewToken();
        }
        // update token
        SmsTokenEntity tokenEntity = entity.get();
        if (LocalDateTime.now().isBefore(tokenEntity.getExpDate())) {
            return tokenEntity.getToken();
        }

        tokenEntity.setToken(getNewToken());
        tokenEntity.setCreatedDate(LocalDateTime.now());
        tokenEntity.setExpDate(LocalDateTime.now().plusMonths(1));
        smsTokenRepository.save(tokenEntity);
        return getNewToken();
    }

    /// agar sms junatgandegi token xali exp bulmagan bulsa shu method ishlaydi, aks xolda getNewToken() methodga utib yangi token oladi

    private String getNewToken() {
        SmsAuthDTO smsAuthDTO = new SmsAuthDTO();
        smsAuthDTO.setEmail(accountLogin);
        smsAuthDTO.setPassword(accountPswd);
        /// login va pswd hardcore da bulmasligi kerak chunki uzgarishi mumkin


        try {
            System.out.println("New Token was obtained");
            String response = restTemplate.postForObject(eskizURL + "api/v1/auth/login", smsAuthDTO, String.class);
            JsonNode parent = new ObjectMapper().readTree(response);
            JsonNode data = parent.get("data");
            return data.get("token").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendResetPasswordSMS(String username, AppLanguage language) {
        String code = RandomUtil.generateRandomCode();
        String message = bundleService.getMessage("sms.message", language);
        message = String.format(message, code);
        sendSms(username, message, code, SmsType.RESET_PASSWORD, language);
    }
}
