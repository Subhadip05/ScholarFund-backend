package com.scholarfund.backend.dto;

import java.time.LocalDate;

public record StudentProfileResponse(
        Long profileId,
        String fullName,
        String email,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String aadhaarNumber
) {}