package code.uz.service;

import code.uz.config.CustomUserDetails;
import code.uz.dto.AppResponse;
import code.uz.dto.auth.ResetPasswordConfirmDTO;
import code.uz.dto.auth.AuthDTO;
import code.uz.dto.ProfileResponseDTO;
import code.uz.dto.auth.RegistrationDTO;
import code.uz.dto.auth.ResetPasswordDTO;
import code.uz.dto.sms.SmsResendDTO;
import code.uz.dto.sms.SmsVerificationDTO;
import code.uz.entity.ProfileEntity;
import code.uz.entity.ProfileRoleEntity;
import code.uz.enums.AppLanguage;
import code.uz.enums.GeneralStatus;
import code.uz.enums.Role;
import code.uz.exception.BadException;
import code.uz.repository.ProfileRepository;
import code.uz.repository.ProfileRoleRepository;
import code.uz.util.EmailUtil;
import code.uz.util.JwtUtil;
import code.uz.util.PhoneUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRoleService profileRoleService;
    private final ProfileService profileService;
    private final ProfileRoleRepository profileRoleRepository;
    private final AuthenticationManager authenticationManager;
    private final ResourceBundleService bundleService;
    private final EmailSendService emailService;
    private final SmsSendService smsSendService;
    private final SmsHistoryService smsHistoryService;
    private final EmailHistoryService emailHistoryService;
    private final AttachService attachService;

    public AppResponse<String> register(RegistrationDTO registrationDTO, AppLanguage language) {
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new BadException(bundleService.getMessage("password.not.match", language));
        }

        Optional<ProfileEntity> byEmailOrPhone = profileRepository.findByUsernameAndVisibleTrue(registrationDTO.getUsername());
        if (byEmailOrPhone.isPresent()) {
            ProfileEntity entity = byEmailOrPhone.get();
            if (entity.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRoleService.deleteRoles(entity.getId());
                profileRepository.delete(entity);
            } else {
                log.warn("Profile: {} already exists", entity.getUsername());
                throw new BadException(bundleService.getMessage("phone.email.exists", language));
            }
        }

        ProfileEntity entity = new ProfileEntity();
        entity.setName(registrationDTO.getName());
        entity.setUsername(registrationDTO.getUsername());
        entity.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        entity.setStatus(GeneralStatus.IN_REGISTRATION);
        profileRepository.save(entity);

        profileRoleService.create(entity.getId(), List.of(Role.ROLE_USER));

        if (EmailUtil.isEmail(registrationDTO.getUsername())) {
            emailService.sendRegistrationEmail(entity.getUsername(), entity.getName(), entity.getId(), language);
            return new AppResponse<>(bundleService.getMessage("email.confirm.sent", language));
        } else if (PhoneUtil.isPhone(registrationDTO.getUsername())) {
            smsSendService.sendRegistrationSms(entity.getUsername(), language);
            return new AppResponse<>(bundleService.getMessage("sms.confirm.sent", language));
        }

        return new AppResponse<>(bundleService.getMessage("invalid.username", language));
    }

    public String registrationEmailVerification(String idToken, AppLanguage language) {
        UUID decodedID = JwtUtil.decodeID(idToken);

        ProfileEntity byId = profileService.getById(decodedID);
        if (byId.getStatus() == GeneralStatus.IN_REGISTRATION) {
            profileRepository.updateStatusById(GeneralStatus.ACTIVE, byId.getId()); /// bu aynan statusni update qiladi, qolgan filed-ga tegishmaydi
        } else {
            throw new BadException(bundleService.getMessage("verification.failed", language));
        }
        return bundleService.getMessage("verification.successful", language);
    }

    public ProfileResponseDTO registrationSmSVerification(SmsVerificationDTO dto, AppLanguage language) {
        Optional<ProfileEntity> profile = profileRepository.findByUsernameAndVisibleTrue(dto.getPhone());
        if (profile.isEmpty()) {
            log.warn("Profile: {} does not exist", dto.getPhone());
            throw new BadException(bundleService.getMessage("profile.not.found", language));
        }
        ProfileEntity entity = profile.get();
        if (!entity.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
            log.error("Verification failed, profile: {} is not in registration", entity.getUsername());
            throw new BadException(bundleService.getMessage("verification.failed", language));
        }
        // code check
        smsHistoryService.checkCode(dto.getPhone(), dto.getCode(), language);
        // make Active
        profileRepository.updateStatusById(GeneralStatus.ACTIVE, entity.getId());
        return getLoginResponse(entity.getId(), entity.getUsername(), entity.getName(), entity.getPhotoId());
        /// birdaniga login response qivaramiz, login page ga otvarish uchun
    }

    public AppResponse<String> SmSVerificationResend(SmsResendDTO dto, AppLanguage language) {
        Optional<ProfileEntity> profile = profileRepository.findByUsernameAndVisibleTrue(dto.getPhone());
        if (profile.isEmpty()) {
            log.warn("Profile: {} does not exist", dto.getPhone());
            throw new BadException(bundleService.getMessage("profile.not.found", language));
        }
        ProfileEntity entity = profile.get();
        if (!entity.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
            throw new BadException(bundleService.getMessage("verification.failed", language));
        }
        // resend sms
        smsSendService.sendRegistrationSms(dto.getPhone(), language);
        return new AppResponse<>(bundleService.getMessage("sms.resend.confirm", language));
    }

    public ProfileResponseDTO login(AuthDTO authDTO, AppLanguage language) {
        try {
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getUsername(), authDTO.getPassword()));

            if (authenticate.isAuthenticated()) {
                CustomUserDetails principal = (CustomUserDetails) authenticate.getPrincipal();

                return getLoginResponse(principal.getId(), principal.getUsername(), principal.getName(), principal.getPhotoId());
            }
        } catch (Exception e) {
            log.warn("Login failed from user: {}", authDTO.getUsername(), e);
            throw new BadException(bundleService.getMessage("invalid.username.password", language));
        }
        throw new BadException(bundleService.getMessage("invalid.username.password", language));
    }

    public AppResponse<String> resetPassword(@Valid ResetPasswordDTO dto, AppLanguage language) {
        Optional<ProfileEntity> profile = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (profile.isEmpty()) {
            log.warn("Profile: {} does not exist", dto.getUsername());
            throw new BadException(bundleService.getMessage("profile.not.found", language));
        }
        ProfileEntity entity = profile.get();
        if (!entity.getStatus().equals(GeneralStatus.ACTIVE)) {
            log.error("Wrong.status: {}", entity.getStatus());
            throw new BadException(bundleService.getMessage("wrong.status", language));
        }

        if (EmailUtil.isEmail(dto.getUsername())) {
            emailService.sendResetPasswordEmail(entity.getUsername(), entity.getName(), language);
        } else if (PhoneUtil.isPhone(dto.getUsername())) {
            smsSendService.sendResetPasswordSMS(entity.getUsername(), language);
        } else {
            log.warn("Invalid email or phone number {}", dto.getUsername());
            return new AppResponse<>(bundleService.getMessage("invalid.username", language));
        }

        String responseMessage = bundleService.getMessage("reset.password.confirm", language);
        return new AppResponse<>(String.format(responseMessage, dto.getUsername()));
    }

    public AppResponse<String> resetPasswordConfirm(@Valid ResetPasswordConfirmDTO dto, AppLanguage language) {
        Optional<ProfileEntity> profile = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (profile.isEmpty()) {
            log.warn("Profile: {} does not exist", dto.getUsername());
            throw new BadException(bundleService.getMessage("profile.not.found", language));
        }
        ProfileEntity entity = profile.get();
        if (!entity.getStatus().equals(GeneralStatus.ACTIVE)) {
            log.error("Wrong.status: {}", entity.getStatus());
            throw new BadException(bundleService.getMessage("wrong.status", language));
        }

        if (EmailUtil.isEmail(dto.getUsername())) {
            emailHistoryService.checkCode(dto.getUsername(), dto.getConfirmCode(), language);
        } else if (PhoneUtil.isPhone(dto.getUsername())) {
            smsHistoryService.checkCode(dto.getUsername(), dto.getConfirmCode(), language);
        } else {
            return new AppResponse<>(bundleService.getMessage("invalid.username", language));
        }
        // update password
        profileRepository.updatePasswordById(entity.getId(), passwordEncoder.encode(dto.getNewPassword()));

        String responseMessage = bundleService.getMessage("password.changed", language);
        return new AppResponse<>(String.format(responseMessage, dto.getUsername()));
    }


    private ProfileResponseDTO getLoginResponse(UUID profileId, String username, String name, String photoId) {
        List<ProfileRoleEntity> allRoleByProfileId = profileRoleRepository.findAllByProfileId(profileId);
        List<Role> roleList = allRoleByProfileId.stream()
                .map(ProfileRoleEntity::getRole)
                .toList();

        // build response
        ProfileResponseDTO responseDTO = new ProfileResponseDTO();
        responseDTO.setName(name);
        responseDTO.setUsername(username);
        responseDTO.setRoles(roleList);
        responseDTO.setAccessToken(JwtUtil.encode(username, profileId, roleList));
        responseDTO.setPhoto(attachService.getPhotoDTO(photoId));

        return responseDTO;
    }
    /// bu metohdni login() va registrationSmSVerification() da ishlatish uchun parameterlarga ikkalasida ham bor fieldlarni berdik va ikkala method-da ham ishlata olamiz endi response sifatida
}
