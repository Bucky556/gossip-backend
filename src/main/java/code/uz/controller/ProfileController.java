package code.uz.controller;


import code.uz.dto.AppResponse;
import code.uz.dto.CodeConfirmDTO;
import code.uz.dto.ProfileResponseDTO;
import code.uz.dto.profile.*;
import code.uz.enums.AppLanguage;
import code.uz.service.ProfileService;
import code.uz.service.ResourceBundleService;
import code.uz.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile Controller", description = "Controller for updating profile details")
public class ProfileController {
    private final ProfileService profileService;
    private final ResourceBundleService bundleService;

    @PutMapping("/update/name")
    @Operation(
            summary = "Update profile name",
            description = "Allows a user to update their name"
    )
    public ResponseEntity<AppResponse<String>> updateName(@RequestBody @Valid ProfileNameDTO dto,
                                                          @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        profileService.updateName(dto);
        return ResponseEntity.ok(new AppResponse<>(bundleService.getMessage("update.name", language)));
    }

    @PutMapping("/update/password")
    @Operation(
            summary = "Update profile password",
            description = "Allows a user to change their password by providing the old and new password."
    )
    public ResponseEntity<AppResponse<String>> updatePassword(@RequestBody @Valid ProfilePasswordDTO passwordDTO,
                                                              @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        profileService.updatePassword(passwordDTO, language);
        return ResponseEntity.ok(new AppResponse<>(bundleService.getMessage("update.password", language)));
    }

    @PutMapping("/update/username")
    @Operation(
            summary = "Request username update",
            description = "Initiates username update process by sending a confirmation code to the new username (email/phone-number)."
    )
    public ResponseEntity<AppResponse<String>> updateUsername(@RequestBody @Valid ProfileUsernameDTO usernameDTO,
                                                              @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        profileService.updateUsername(usernameDTO, language);
        String responseMessage = bundleService.getMessage("code.send", language);
        return ResponseEntity.ok(new AppResponse<>(String.format(responseMessage, usernameDTO.getUsername())));
    }

    @PutMapping("/update/username/confirm")
    @Operation(
            summary = "Confirm username update",
            description = "Confirms the username update process by validating the confirmation code previously sent."
    )
    public ResponseEntity<AppResponse<String>> updateUsernameConfirm(@RequestBody @Valid CodeConfirmDTO confirmDTO,
                                                                     @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        return ResponseEntity.ok(profileService.confirmCode(confirmDTO, language));
    }

    @PutMapping("/update/photo")
    @Operation(
            summary = "Update profile photo",
            description = "Allows a user to update their profile picture by providing a new photo ID."
    )
    public ResponseEntity<AppResponse<String>> updatePhoto(@RequestBody @Valid ProfileImageUpdateDTO dto,
                                                           @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        return ResponseEntity.ok(profileService.updatePhoto(dto.getPhotoId(), language));
    }

    @PostMapping("/filter")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Filter profiles",
            description = "This API allows admin to search profiles that are visible true with pagination"
    )
    public ResponseEntity<PageImpl<ProfileResponseDTO>> filter(@RequestBody ProfileFilterDTO filterDTO,
                                                               @RequestParam(value = "page", defaultValue = "1") int page,
                                                               @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(profileService.filter(filterDTO, PageUtil.getCurrentPage(page), size));
    }

    @PutMapping("/status/{profileId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Change status of profiles",
            description = "This API allows admin to change status of profiles. (active, block)"
    )
    public ResponseEntity<AppResponse<String>> changeStatus(@PathVariable("profileId") UUID profileId,
                                                            @RequestBody ProfileStatusDTO dto,
                                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        return ResponseEntity.ok(profileService.changeStatus(profileId, dto.getStatus(), language));
    }

    @DeleteMapping("/delete/{profileId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Delete Profile",
            description = "This API allows admin to profile"
    )
    public ResponseEntity<AppResponse<String>> deleteProfile(@PathVariable("profileId") UUID profileId,
                                                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        return ResponseEntity.ok(profileService.delete(profileId, language));
    }
}
