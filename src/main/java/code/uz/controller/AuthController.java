package code.uz.controller;

import code.uz.dto.AppResponse;
import code.uz.dto.auth.ResetPasswordConfirmDTO;
import code.uz.dto.auth.AuthDTO;
import code.uz.dto.ProfileResponseDTO;
import code.uz.dto.auth.RegistrationDTO;
import code.uz.dto.auth.ResetPasswordDTO;
import code.uz.dto.sms.SmsResendDTO;
import code.uz.dto.sms.SmsVerificationDTO;
import code.uz.enums.AppLanguage;
import code.uz.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller",description = "Controller for authorization and authentication")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/registration")
    @Operation(summary = "Profile registration", description = "Api for registering")
    public ResponseEntity<AppResponse<String>> registration(@RequestBody @Valid RegistrationDTO registrationDTO,
                                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        log.info("Registration request received from {}", registrationDTO.getUsername());
        return ResponseEntity.ok(authService.register(registrationDTO, language));
    }

    @GetMapping("/registration/email-verification/{IdToken}")
    @Operation(summary = "Profile email verification", description = "Api for email verification")
    public ResponseEntity<String> emailVerification(@PathVariable("IdToken") String profileId,
                                                    @RequestParam(value = "lang", defaultValue = "UZ") AppLanguage language) {
        log.info("Email verification request received from {}", profileId);
        return ResponseEntity.ok(authService.registrationEmailVerification(profileId, language));
    }

    @PostMapping("/registration/sms-verification")
    @Operation(summary = "Profile sms verification", description = "Api for sms verification")
    public ResponseEntity<ProfileResponseDTO> smsVerification(@RequestBody @Valid SmsVerificationDTO dto,
                                                              @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        log.info("Sms verification received from {}" , dto.getPhone());
        return ResponseEntity.ok(authService.registrationSmSVerification(dto, language));
    }

    @PostMapping("/registration/sms-verification/resend")
    @Operation(summary = "Sms resending", description = "Api for resending sms verification code")
    public ResponseEntity<AppResponse<String>> smsVerificationResend(@RequestBody @Valid SmsResendDTO dto,
                                                                     @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        log.info("Sms verification resend request received from {}" , dto.getPhone());
        return ResponseEntity.ok(authService.SmSVerificationResend(dto, language));
    }

    @PostMapping("/login")
    @Operation(summary = "Profile login", description = "Api for login")
    public ResponseEntity<ProfileResponseDTO> login(@RequestBody @Valid AuthDTO authDTO,
                                                    @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        log.info("Login request received from {}", authDTO.getUsername());
        return ResponseEntity.ok(authService.login(authDTO, language));
    }

    @PostMapping("/reset/password")
    @Operation(summary = "Profile reset password", description = "Api for reset password")
    public ResponseEntity<AppResponse<String>> resetPassword(@RequestBody @Valid ResetPasswordDTO dto,
                                                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        log.info("Reset password request received from {}", dto.getUsername());
        return ResponseEntity.ok(authService.resetPassword(dto, language));
    }

    @PostMapping("/reset/password/confirm")
    @Operation(summary = "Reset password confirm", description = "Api for confirm reset password code")
    public ResponseEntity<AppResponse<String>> resetPasswordConfirm(@RequestBody @Valid ResetPasswordConfirmDTO dto,
                                                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        return ResponseEntity.ok(authService.resetPasswordConfirm(dto, language));
    }
}
