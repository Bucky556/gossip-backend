package code.uz.mapper;

import code.uz.enums.GeneralStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProfileMapper {
        UUID getId();
        String getUsername();
        String getName();
        LocalDateTime getCreatedDate();
        GeneralStatus getStatus();
        String getPhotoId();
        Long getPostCount();
        String getRoleList();
}
