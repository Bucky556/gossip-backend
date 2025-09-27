package code.uz.controller;

import code.uz.dto.AppResponse;
import code.uz.dto.post.PostAdminFilterDTO;
import code.uz.dto.post.PostCreateDTO;
import code.uz.dto.post.PostDTO;
import code.uz.dto.post.PostFilterDTO;
import code.uz.enums.AppLanguage;
import code.uz.service.PostService;
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


@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
@Tag(name = "Post Controller", description = "APIs for managing posts (CRUD, filter)")
public class PostController {
    private final PostService postService;
    private final ResourceBundleService bundleService;

    @PostMapping("/create")
    @Operation(summary = "Create a new post", description = "Allows a user to create a new post with title, content and photo.")
    public ResponseEntity<AppResponse<PostDTO>> create(@RequestBody @Valid PostCreateDTO dto,
                                                       @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        PostDTO createdPost = postService.create(dto);
        return ResponseEntity.ok(
                new AppResponse<>(createdPost, bundleService.getMessage("created.post", language))
        );
    }

    @GetMapping("/list")
    @Operation(summary = "Get Post List of user", description = "Finds all visible posts created by the currently authenticated user, ordered by created date desc.")
    public ResponseEntity<PageImpl<PostDTO>> postList(@RequestParam(value = "page", defaultValue = "1") int page,
                                                      @RequestParam(value = "size", defaultValue = "6") int size) {
        return ResponseEntity.ok(postService.getProfilePostList(PageUtil.getCurrentPage(page), size));
    }

    @GetMapping("/get/by-id/{id}")
    @Operation(summary = "Get post by ID", description = "Finds post by ID and shows its full info")
    public ResponseEntity<PostDTO> getPostById(@PathVariable String id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update post", description = "Updates an existing post by ID. Only the post owner or an admin can update it.")
    public ResponseEntity<AppResponse<PostDTO>> updateById(@PathVariable String id, @RequestBody @Valid PostCreateDTO dto,
                                                           @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        PostDTO updatedPost = postService.update(id, dto);
        return ResponseEntity.ok(new AppResponse<>(updatedPost, bundleService.getMessage("updated.post", language)));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete post", description = "Marks a post as invisible (soft delete). Only the post owner or an admin can delete it.")
    public ResponseEntity<AppResponse<String>> deleteById(@PathVariable String id,
                                                          @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        postService.delete(id);
        return ResponseEntity.ok(new AppResponse<>(bundleService.getMessage("deleted.post", language)));
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter posts", description = "Filters posts based on search query, pagination")
    public ResponseEntity<PageImpl<PostDTO>> filter(@RequestBody @Valid PostFilterDTO dto,
                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.filter(dto, PageUtil.getCurrentPage(page), size));
    }

    @PostMapping("/admin/filter/posts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Filter profile posts for admin", description = "Filtering posts for admin")
    public ResponseEntity<PageImpl<PostDTO>> filterPosts(@RequestBody PostAdminFilterDTO dto,
                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                         @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.filterPosts(dto, PageUtil.getCurrentPage(page), size));
    }
}
