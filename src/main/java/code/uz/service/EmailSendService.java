package code.uz.service;

import code.uz.enums.AppLanguage;
import code.uz.enums.EmailType;
import code.uz.exception.BadException;
import code.uz.util.JwtUtil;
import code.uz.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class EmailSendService {
    @Value("${spring.mail.username}")
    private String fromEmail;
    private final JavaMailSender mailSender;
    @Value("${server.domain}")
    private String domain;
    private final EmailHistoryService emailHistoryService;
    private final ResourceBundleService bundleService;

    private void sendMimeEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            CompletableFuture.runAsync(() ->  // emailga message ketish vaqtini tejaydi thread orqali
                    mailSender.send(message)
            );
        } catch (MessagingException e) {
            throw new BadException("Failed to send email");
        }
    }

    public void sendRegistrationEmail(String toEmail, String name, UUID profileId, AppLanguage language) {
        try {
            String emailFile = switch (language) {
                case EN -> "email-body_en.html";
                case RU -> "email-body_ru.html";
                default -> "email-body_uz.html";
            };

            ClassPathResource resource = new ClassPathResource(emailFile);
            String htmlContent = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            String htmlBody = htmlContent
                    .replace("{{name}}", name)
                    .replace("{{profileId}}", JwtUtil.encodeID(profileId)) // for Security
                    .replace("{{domain}}", domain);

            String subject = switch (language) {
                case RU -> "Подтверждение регистрации";
                case EN -> "Registration Confirmation";
                default -> "Ro‘yxatdan o‘tishni tasdiqlash";
            };

            sendMimeEmail(toEmail, subject, htmlBody);
        } catch (IOException e) {
            throw new BadException("Failed to load email template");
        }
    }

    public void sendResetPasswordEmailBody(String toEmail, String name, AppLanguage language) {
        try {
            String code = RandomUtil.generateRandomCode();

            String emailFile = switch (language) {
                case EN -> "reset_pswd_email_en.html";
                case RU -> "reset_pswd_email_ru.html";
                default -> "reset_pswd_email_uz.html";
            };

            ClassPathResource resource = new ClassPathResource(emailFile);
            String htmlContent = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            String htmlBody = htmlContent
                    .replace("{{name}}", name)
                    .replace("{{code}}", code);
            String subject = switch (language) {
                case EN -> "Reset password confirmation";
                case RU -> "Подтверждение сброса пароля";
                default -> "Parolni tiklashni tasdiqlash";
            };

            sendMimeEmail(toEmail, subject, htmlBody);
            checkAndSaveEmailToDB(toEmail, language, code);
        } catch (IOException e) {
            throw new BadException("Failed to load email template");
        }
    }

    private void checkAndSaveEmailToDB(String toEmail, AppLanguage language, String code) {
        // save database
        emailHistoryService.create(toEmail, code, EmailType.RESET_PASSWORD);
        // count mail
        Long emailCount = emailHistoryService.getEmailCountWhileSending(toEmail);
        Integer emailLimit = 3;
        if (emailCount > emailLimit) {
            throw new BadException(bundleService.getMessage("email.limit.exceeded", language));
        }
        // check code
        emailHistoryService.checkCode(toEmail, code, language);
    }

    public void sendResetPasswordEmail(String username, String name, AppLanguage language) {
        sendResetPasswordEmailBody(username, name, language);
    }

    public void sendEmailForUpdateUsername(@NotBlank(message = "Email or phone required") String email, String name, AppLanguage language) {
        try {
            String code = RandomUtil.generateRandomCode();

            String emailFile = switch (language) {
                case RU -> "update_username_email_ru.html";
                case EN -> "update_username_email_en.html";
                default -> "update_username_email_uz.html";
            };

            ClassPathResource resource = new ClassPathResource(emailFile);
            String htmlContent = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            String htmlBody = htmlContent
                    .replace("{{name}}", name)
                    .replace("{{code}}", code);
            String subject = switch (language) {
                case EN -> "Update Username confirmation";
                case RU -> "Подтверждение обновления имени пользователя";
                default -> "Foydalanuvchi nomini yangilashni tasdiqlash";
            };

            sendMimeEmail(email, subject, htmlBody);

            emailHistoryService.create(email, code, EmailType.UPDATE_USERNAME);
            // count mail
            Long emailCount = emailHistoryService.getEmailCountWhileSending(email);
            Integer emailLimit = 3;
            if (emailCount >= emailLimit) {
                throw new BadException(bundleService.getMessage("email.limit.exceeded", language));
            }
            // check code
            emailHistoryService.checkCode(email, code, language);
        } catch (IOException e) {
            throw new BadException("Failed to load email template");
        }
    }
}
