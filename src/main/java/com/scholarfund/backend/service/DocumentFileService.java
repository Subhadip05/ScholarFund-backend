package com.scholarfund.backend.service;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.dto.ConfirmUploadRequest;
import com.scholarfund.backend.dto.DocumentUploadResponse;
import com.scholarfund.backend.dto.PresignedUploadResponse;
import com.scholarfund.backend.entity.DocumentFile;
import com.scholarfund.backend.repository.DocumentFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentFileService {

    private final S3Service s3Service;
    private final DocumentFileRepository documentFileRepository;

    @Transactional
    public DocumentUploadResponse uploadDocument(MultipartFile file, String folder) {
        // 1. Upload to AWS S3
        String s3Key = s3Service.uploadFile(file, folder);

        // 2. Save the metadata to the database
        DocumentFile document = DocumentFile.builder()
                .fileName(file.getOriginalFilename())
                .s3Key(s3Key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        DocumentFile savedDocument = documentFileRepository.saveAndFlush(document);
        String documentUrl = s3Service.generatePresignedUrl(s3Key);

        // 4. Return both ID and URL
        return new DocumentUploadResponse(savedDocument.getId(), documentUrl);
    }

    public String getDocumentViewUrl(Long documentId) {
        DocumentFile document = documentFileRepository.findById(documentId)
                .orElseThrow(() -> new ScholarFundException("Document not found", ErrorCode.NOT_FOUND, null));

        return s3Service.generatePresignedUrl(document.getS3Key());
    }

    // Phase 1: Generate pre-signed URL for direct upload
    public PresignedUploadResponse initiateUpload(String originalFileName, String folder) {
        String fileExtension = (originalFileName != null && originalFileName.contains("."))
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : "";
        String s3Key = folder + "/" + UUID.randomUUID() + fileExtension;

        String uploadUrl = s3Service.generatePresignedUploadUrl(s3Key);
        return new PresignedUploadResponse(uploadUrl, s3Key);
    }

    // Phase 2: Save metadata once frontend finishes direct upload to S3
    @Transactional
    public DocumentUploadResponse confirmUpload(ConfirmUploadRequest request) {
        DocumentFile document = DocumentFile.builder()
                .fileName(request.fileName())
                .s3Key(request.s3Key())
                .contentType(request.contentType())
                .fileSize(request.fileSize())
                .build();

        DocumentFile saved = documentFileRepository.save(document);
        String viewUrl = s3Service.generatePresignedUrl(saved.getS3Key());

        return new DocumentUploadResponse(saved.getId(), viewUrl);
    }
}