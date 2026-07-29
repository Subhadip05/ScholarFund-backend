package com.scholarfund.backend.dto;

import java.time.LocalDate;

public record StudentProfileResponse(
        Long profileId,
        String fullName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String aadhaarNumber,
        Boolean isWestBengalResident,
        Boolean isApplicationSubmitted,

        // AWS Presigned URLs for frontend display
        String aadhaarDocumentUrl,
        String selfImageDocUrl,
        String selfSignatureUrl
) {}