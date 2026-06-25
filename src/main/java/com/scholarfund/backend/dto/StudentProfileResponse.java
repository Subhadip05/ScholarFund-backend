package com.scholarfund.backend.dto;

import java.time.LocalDate;

public record StudentProfileResponse(
        Long profileId,
        String fullName,
        String email,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String aadhaarNumber,
        Double hsMarksPercentage,
        Double annualIncome,
        Boolean isWestBengalResident,
        String bankAccountNumber,
        String ifscCode,

        // AWS Presigned URLs for frontend display
        String aadhaarDocumentUrl,
        String incomeCertificateUrl,
        String hsMarksheetUrl,
        String bankPassbookUrl

) {}