package code.uz.service;

import code.uz.dto.FilterResultDTO;
import code.uz.dto.ProfileResponseDTO;
import code.uz.dto.post.PostAdminFilterDTO;
import code.uz.dto.post.PostCreateDTO;
import code.uz.dto.post.PostDTO;
import code.uz.dto.post.PostFilterDTO;
import code.uz.entity.PostEntity;
import code.uz.entity.ProfileEntity;
import code.uz.enums.Role;
import code.uz.exception.BadException;
import code.uz.repository.FilterPostRepository;
import code.uz.repository.PostRepository;
import code.uz.repository.ProfileRepository;
import code.uz.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final AttachService attachService;
    private final FilterPostRepository filterRepository;

    public PostDTO create(PostCreateDTO dto) {
        UUID profileID = SecurityUtil.getID();
        Optional<ProfileEntity> profile = profileRepository.findByIdAndVisibleTrue(profileID);
        if (profile.isEmpty()) {
            throw new BadException("Profile not found");
        }
        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(dto.getContent());
        postEntity.setPhotoId(dto.getPhoto().getId());
        postEntity.setProfileId(profile.get().getId());
        postRepository.save(postEntity);

        return toDTO(postEntity);
    }

    public PageImpl<PostDTO> getProfilePostList(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        UUID profileID = SecurityUtil.getID();
        Page<PostEntity> allPosts = postRepository
                .findAllByProfileIdAndVisibleTrueOrderByCreatedDateDesc(profileID, pageRequest);

        if (allPosts == null || allPosts.isEmpty()) {
            return new PageImpl<>(Collections.emptyList()); // bosh list qaytaradi
        }

        List<PostDTO> dtoList = allPosts.getContent().stream()
                .map(this::toDTO)
                .toList();

        long totalElements = allPosts.getTotalElements();

        return new PageImpl<>(dtoList, pageRequest, totalElements);
    }

    public PostDTO getById(String id) {
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new BadException("Post not found"));
        return toAllDTO(postEntity);
    }

    @Transactional
    public PostDTO update(String id, PostCreateDTO dto) {
        PostEntity postEntity = getPostById(id);
        if (postEntity.getVisible().equals(false)) {
            throw new BadException("This post cannot be updated");
        }

        UUID profileId = SecurityUtil.getID();
        if (!SecurityUtil.hasRole(Role.ROLE_ADMIN) && !postEntity.getProfileId().equals(profileId)) {
            throw new BadException("You do not have permission to update this post");
        }

        String oldPhotoId = null;
        if (!dto.getPhoto().getId().equals(postEntity.getPhotoId())) {
            oldPhotoId = postEntity.getPhotoId();
        }
        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(dto.getContent());
        postEntity.setPhotoId(dto.getPhoto().getId());
        PostEntity saved = postRepository.save(postEntity);
        if (oldPhotoId != null) {
            attachService.delete(oldPhotoId);
        }
        return toDTO(saved);
    }

    public void delete(String id) {
        PostEntity postEntity = getPostById(id);
        UUID profileId = SecurityUtil.getID();
        if (!SecurityUtil.hasRole(Role.ROLE_ADMIN) && !postEntity.getProfileId().equals(profileId)) {
            throw new BadException("You do not have permission");
        }
        postRepository.changeVisibleById(postEntity.getId());
    }

    public PageImpl<PostDTO> filter(PostFilterDTO dto, int page, int size) {
        FilterResultDTO<PostEntity> filter = filterRepository.filter(dto, page, size);
        List<PostDTO> dtoList = filter
                .getList()
                .stream()
                .map(this::toDTO)
                .toList();

        return new PageImpl<>(dtoList, PageRequest.of(page, size), filter.getTotalCount());
    }

    private PostEntity getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new BadException("Post not found"));
    }

    public PageImpl<PostDTO> filterPosts(PostAdminFilterDTO dto, int currentPage, int size) {
        FilterResultDTO<Object[]> filterPosts = filterRepository.filterAdminPosts(dto, currentPage, size);
        List<PostDTO> dtoList = filterPosts.getList()
                .stream()
                .map(this::toDTOMapper)
                .toList();

        return new PageImpl<>(dtoList, PageRequest.of(currentPage, size), filterPosts.getTotalCount());
    }

    private PostDTO toDTOMapper(Object[] obj) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId((String) obj[0]);
        postDTO.setTitle((String) obj[1]);
        postDTO.setCreatedDate((LocalDateTime) obj[2]);
        if (obj[3] != null) {
            postDTO.setPhoto(attachService.getPhotoDTO((String) obj[3]));
        }

        ProfileResponseDTO profileDTO = new ProfileResponseDTO();
        profileDTO.setId((UUID) obj[4]);
        profileDTO.setUsername((String) obj[5]);
        profileDTO.setName((String) obj[6]);

        postDTO.setProfile(profileDTO);
        return postDTO;
    }

    private PostDTO toDTO(PostEntity postEntity) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(postEntity.getId());
        postDTO.setTitle(postEntity.getTitle());
        //Content bu yerda hozircha kerak emas chunki biz postlarni kurekanimizda har doim content kurinmaydi, qachonki postni uzini batafsil kurmochi bulsak content kerak buladi
        //postDTO.setContent(postEntity.getContent());
        postDTO.setCreatedDate(postEntity.getCreatedDate());
        postDTO.setPhoto(attachService.getPhotoDTO(postEntity.getPhotoId()));
        return postDTO;
    }

    private PostDTO toAllDTO(PostEntity postEntity) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(postEntity.getId());
        postDTO.setTitle(postEntity.getTitle());
        postDTO.setContent(postEntity.getContent());
        postDTO.setCreatedDate(postEntity.getCreatedDate());
        postDTO.setPhoto(attachService.getPhotoDTO(postEntity.getPhotoId()));
        return postDTO;
    }
}
