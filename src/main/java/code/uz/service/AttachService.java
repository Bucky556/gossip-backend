package code.uz.service;

import code.uz.dto.AttachDTO;
import code.uz.entity.AttachEntity;
import code.uz.exception.BadException;
import code.uz.repository.AttachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttachService {
    @Value("${attach.upload.folder}")
    private String folderName;
    @Value("${attach.url}")
    private String attachUrl;
    private final AttachRepository attachRepository;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "pdf", "docx");

    public AttachDTO upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadException("File is too big");
        }

        String extensions = getExtension(Objects.requireNonNull(file.getOriginalFilename()));
        if (!ALLOWED_EXTENSIONS.contains(extensions.toLowerCase())) {
            throw new BadException("Unsupported file type");
        }

        try {
            String pathFolder = getYmDString();
            String key = UUID.randomUUID().toString();
            String extension = getExtension(Objects.requireNonNull(file.getOriginalFilename()));

            File folder = new File(folderName + "/" + pathFolder); // folder/**
            if (!folder.exists()) {
                boolean t = folder.mkdirs();
            }
            // save to system
            byte[] bytes = file.getBytes();
            Path path = Paths.get(folderName + "/" + pathFolder + "/" + key + "." + extension);
            Files.write(path, bytes);
            // save to db
            AttachEntity attachEntity = new AttachEntity();
            attachEntity.setId(key + "." + extension);
            attachEntity.setPath(pathFolder);
            attachEntity.setSize(file.getSize());
            attachEntity.setOriginalName(file.getOriginalFilename());
            attachEntity.setExtension(extension);
            attachRepository.save(attachEntity);

            return toDTO(attachEntity);
        } catch (Exception e) {
            throw new BadException("Bad Request");
        }
    }

    public ResponseEntity<Resource> open(String id) {
        AttachEntity attachEntity = getEntity(id);
        Path filePath = Paths.get(folderName + "/" + attachEntity.getPath() + "/" + attachEntity.getId()).normalize();  // normalize urtadegi keraksiz narsalarni olib tashlaydi
        Resource resource = null;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BadException("File not found" + id);
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachEntity.getOriginalName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private AttachEntity getEntity(String id) {
        Optional<AttachEntity> byId = attachRepository.findById(id);
        if (byId.isEmpty()) {
            throw new BadException("Attach not found");
        }
        return byId.get();
    }

    private AttachDTO toDTO(AttachEntity attachEntity) {
        AttachDTO attachDTO = new AttachDTO();
        attachDTO.setId(attachEntity.getId());
        attachDTO.setSize(attachEntity.getSize());
        attachDTO.setOriginalName(attachEntity.getOriginalName());
        attachDTO.setExtension(attachEntity.getExtension());
        attachDTO.setCreatedDate(attachEntity.getCreatedDate());
        attachDTO.setUrl(openURL(attachEntity.getId()));
        return attachDTO;
    }

    private String openURL(String fileName) {
        return attachUrl + "/open/" + fileName;
    }

    private String getExtension(String originalFilename) {
        int lastIndex = originalFilename.lastIndexOf(".");
        return originalFilename.substring(lastIndex + 1);
    }

    private String getYmDString() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DATE);
        return year + "/" + month + "/" + day;
    }

    public void delete(String id) {
        AttachEntity entity = getEntity(id);
        attachRepository.changeVisibleById(entity.getId());
        File file = new File(folderName + "/" + entity.getPath() + "/" + entity.getId());
        boolean delete = false;
        if (file.exists()) {
            delete = file.delete();
        }
    }

    public AttachDTO getPhotoDTO(String photoId) {
        if (photoId == null) return null;
        AttachDTO attachDTO = new AttachDTO();
        attachDTO.setId(photoId);
        attachDTO.setUrl(openURL(photoId));
        return attachDTO;
    }
}
