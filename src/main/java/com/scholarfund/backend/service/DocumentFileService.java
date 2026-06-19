package com.scholarfund.backend.service;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.entity.DocumentFile;
import com.scholarfund.backend.repository.DocumentFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentFileService {

    private final S3Service s3Service;
    private final DocumentFileRepository documentFileRepository;

    @Transactional
    public DocumentFile uploadDocument(MultipartFile file, String folder) {
        // 1. Upload to AWS S3
        String s3Key = s3Service.uploadFile(file, folder);

        // 2. Save the metadata to the database
        DocumentFile document = DocumentFile.builder()
                .fileName(file.getOriginalFilename())
                .s3Key(s3Key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        return documentFileRepository.save(document);
    }

    public String getDocumentViewUrl(Long documentId) {
        DocumentFile document = documentFileRepository.findById(documentId)
                .orElseThrow(() -> new ScholarFundException("Document not found", ErrorCode.NOT_FOUND, null));

        return s3Service.generatePresignedUrl(document.getS3Key());
    }
}