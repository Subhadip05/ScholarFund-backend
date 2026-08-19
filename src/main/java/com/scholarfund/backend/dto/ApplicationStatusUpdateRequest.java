package com.scholarfund.backend.dto;

import com.scholarfund.backend.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusUpdateRequest(
        @NotNull(message = "Status action is required")
        ApplicationStatus statusAction,
        String actionRemarks
) {}