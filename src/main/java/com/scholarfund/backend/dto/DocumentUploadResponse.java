package com.scholarfund.backend.dto;

public record DocumentUploadResponse(
        Long documentId,
        String documentUrl
) {}