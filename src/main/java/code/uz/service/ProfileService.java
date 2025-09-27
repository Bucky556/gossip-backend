package code.uz.service;


import code.uz.dto.AppResponse;
import code.uz.dto.CodeConfirmDTO;
import code.uz.dto.ProfileResponseDTO;
import code.uz.dto.profile.ProfileFilterDTO;
import code.uz.dto.profile.ProfileNameDTO;
import code.uz.dto.profile.ProfilePasswordDTO;
import code.uz.dto.profile.ProfileUsernameDTO;
import code.uz.entity.ProfileEntity;
import code.uz.entity.ProfileRoleEntity;
import code.uz.enums.AppLanguage;
import code.uz.enums.GeneralStatus;
import code.uz.enums.Role;
import code.uz.exception.BadException;
import code.uz.mapper.ProfileMapper;
import code.uz.repository.ProfileRepository;
import code.uz.repository.ProfileRoleRepository;
import code.uz.util.EmailUtil;
import code.uz.util.JwtUtil;
import code.uz.util.PhoneUtil;
import code.uz.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ResourceBundleService bundleService;
    private final PasswordEncoder passwordEncoder;
    private final SmsSendService smsSendService;
    protected final EmailSendService emailSendService;
    private final SmsHistoryService smsHistoryService;
    private final EmailHistoryService emailHistoryService;
    private final ProfileRoleRepository profileRoleRepository;
    private final AttachService attachService;

    public void updateName(ProfileNameDTO detailDTO) {
        UUID profileID = SecurityUtil.getID();
        profileRepository.updateNameById(profileID, detailDTO.getName());
    }

    public void updatePassword(@Valid ProfilePasswordDTO dto, AppLanguage language) {
        UUID profileID = SecurityUtil.getID();
        ProfileEntity profileEntity = getById(profileID);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), profileEntity.getPassword())) {
            throw new BadException(bundleService.getMessage("wrong.current.pswd", language));
        }

        profileRepository.updatePasswordById(profileID, passwordEncoder.encode(dto.getNewPassword()));
    }

    public void updateUsername(@Valid ProfileUsernameDTO dto, AppLanguage language) {
        UUID profileId = SecurityUtil.getID();
        ProfileEntity profileEntity = getById(profileId);
        if (profileEntity.getUsername().equals(dto.getUsername())) {
            throw new BadException(bundleService.getMessage("phone.email.exists", language));
        }

        if (PhoneUtil.isPhone(dto.getUsername())) {
            smsSendService.sendSmsForUpdateUsername(dto.getUsername(), language);
        } else if (EmailUtil.isEmail(dto.getUsername())) {
            emailSendService.sendEmailForUpdateUsername(dto.getUsername(), profileEntity.getName(), language);
        }

        profileRepository.updateUpd_UsernameById(profileId, dto.getUsername());
    }

    public AppResponse<String> confirmCode(@Valid CodeConfirmDTO confirmDTO, AppLanguage language) {
        UUID profileId = SecurityUtil.getID();
        ProfileEntity profileEntity = getById(profileId);

        if (PhoneUtil.isPhone(profileEntity.getUpdatedUsername())) {
            smsHistoryService.checkCode(profileEntity.getUpdatedUsername(), confirmDTO.getCode(), language);
        } else if (EmailUtil.isEmail(profileEntity.getUpdatedUsername())) {
            emailHistoryService.checkCode(profileEntity.getUpdatedUsername(), confirmDTO.getCode(), language);
        }

        profileRepository.updateUsernameById(profileId, profileEntity.getUpdatedUsername());
        // response by changing token into new username
        List<ProfileRoleEntity> roles = profileRoleRepository.findAllByProfileId(profileId);
        List<Role> roleList = roles.stream()
                .map(ProfileRoleEntity::getRole)
                .toList();
        String jwt = JwtUtil.encode(profileEntity.getUpdatedUsername(), profileEntity.getId(), roleList);
        return new AppResponse<>(jwt, bundleService.getMessage("update.username", language));
    }

    public AppResponse<String> updatePhoto(String photoId, AppLanguage language) {
        UUID profileId = SecurityUtil.getID();
        ProfileEntity profileEntity = getById(profileId);
        profileRepository.updatePhoto(profileId, photoId);

        if (profileEntity.getPhotoId() != null && !profileEntity.getPhotoId().equals(photoId)) {
            attachService.delete(profileEntity.getPhotoId());  // change visible=false and delete from system
        }
        return new AppResponse<>(bundleService.getMessage("photo.updated", language));
    }

    public ProfileEntity getById(UUID id) {
        return profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new BadException("User not found"));
    }

    public PageImpl<ProfileResponseDTO> filter(ProfileFilterDTO filterDTO, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ProfileMapper> profileEntities = null;
        if (filterDTO.getQuery() == null || filterDTO.getQuery().isEmpty()) {
            profileEntities = profileRepository.filterAllWithPostCount(pageRequest);
        } else {
            profileEntities = profileRepository.filterProfileWithPostCount("%" + filterDTO.getQuery().toLowerCase() + "%", pageRequest);
        }

        List<ProfileResponseDTO> resultList = profileEntities.getContent()
                .stream()
                .map(this::toDTOMapper)
                .toList();

        return new PageImpl<>(resultList, pageRequest, profileEntities.getTotalElements());
    }

    // postCount qushilgani uchun shunaqa toDTOMapper() yozildi
    public ProfileResponseDTO toDTOMapper(ProfileMapper profileMapper) {
        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(profileMapper.getId());
        dto.setUsername(profileMapper.getUsername());
        dto.setName(profileMapper.getName());
        if (profileMapper.getRoleList() != null) {
            List<Role> roleList = Arrays.stream(profileMapper.getRoleList().split(","))
                    .map(Role::valueOf)
                    .toList();
            dto.setRoles(roleList);
        }
        dto.setPhoto(attachService.getPhotoDTO(profileMapper.getPhotoId()));
        dto.setStatus(profileMapper.getStatus());
        dto.setCreatedDate(profileMapper.getCreatedDate());
        dto.setPostCount(profileMapper.getPostCount());
        return dto;
    }

    public AppResponse<String> changeStatus(UUID profileId, GeneralStatus status, AppLanguage language) {
        profileRepository.updateStatusById(status, profileId);
        return new AppResponse<>(bundleService.getMessage("status.updated", language));
    }

    public AppResponse<String> delete(UUID profileId, AppLanguage language) {
        profileRepository.changeVisibleById(profileId);
        return new AppResponse<>(bundleService.getMessage("delete.profile", language));
    }

    public ProfileResponseDTO toDTO(ProfileEntity profileEntity) {
        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(profileEntity.getId());
        dto.setUsername(profileEntity.getUsername());
        dto.setName(profileEntity.getName());
        if (profileEntity.getRole() != null) {
            List<Role> roleList = profileEntity.getRole()
                    .stream()
                    .map(ProfileRoleEntity::getRole)
                    .toList();
            dto.setRoles(roleList);
        }
        dto.setPhoto(attachService.getPhotoDTO(profileEntity.getPhotoId()));
        dto.setStatus(profileEntity.getStatus());
        dto.setCreatedDate(profileEntity.getCreatedDate());
        return dto;
    }
}
