package code.uz.controller;

import code.uz.dto.AttachDTO;
import code.uz.service.AttachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/attach")
@RequiredArgsConstructor
@Tag(name = "Attach Controller", description = "Controller for attaching files (upload/open)")
public class AttachController {
    private final AttachService attachService;

    @PostMapping("/upload")
    @Operation(
            summary = "Upload a file",
            description = "Uploads a file to the server and returns its metadata (ID, URL, name, size ...)."
    )
    public ResponseEntity<AttachDTO> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(attachService.upload(file));
    }

    @GetMapping("/open/{fileName}")
    @Operation(
            summary = "Open a file",
            description = "Api for opening file often by photo ID"
    )
    public ResponseEntity<Resource> open(@PathVariable String fileName) {
        return attachService.open(fileName);
    }
}
