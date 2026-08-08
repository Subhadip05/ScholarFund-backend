package com.scholarfund.backend.controller;

import com.scholarfund.backend.common.model.ApiResponse;
import com.scholarfund.backend.dto.DocumentUploadResponse;
import com.scholarfund.backend.service.DocumentFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentFileController {

    private final DocumentFileService documentFileService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {

        log.info("Background file upload initiated for folder: {}", folder);

        DocumentUploadResponse response = documentFileService.uploadDocument(file, folder);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "File uploaded successfully", response)
        );
    }

    // This endpoint fetches the secure AWS link so the frontend can display the image/PDF
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view/{documentId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> viewDocument(@PathVariable Long documentId) {

        String presignedUrl = documentFileService.getDocumentViewUrl(documentId);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "URL generated", Map.of("url", presignedUrl))
        );
    }
}