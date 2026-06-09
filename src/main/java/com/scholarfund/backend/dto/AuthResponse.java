package com.scholarfund.backend.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String role,
        String fullName,
        String email
) {}