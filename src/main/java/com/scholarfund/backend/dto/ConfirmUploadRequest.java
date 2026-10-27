package com.scholarfund.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmUploadRequest(
        @NotBlank String fileName,
        @NotBlank String s3Key,
        @NotBlank String contentType,
        @NotNull Long fileSize
) {}
