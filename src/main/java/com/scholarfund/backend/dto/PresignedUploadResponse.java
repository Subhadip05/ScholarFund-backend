package com.scholarfund.backend.dto;

public record PresignedUploadResponse(
        String uploadUrl,
        String s3Key
) {}
