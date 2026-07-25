package com.scholarfund.backend.dto;

import java.time.LocalDateTime;

public record ApplicationHistoryDto(
        String actionTaken,
        String actionBy,
        String actorRole,
        String remarks,
        LocalDateTime actionTime
) {}