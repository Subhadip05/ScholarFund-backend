package com.scholarfund.backend.dto;

public record InstituteProfileResponse(
        Long profileId,
        String email, // from User table
        String instituteName,
        String collegeCode,
        String universityAffiliation,
        String principalName,
        String address,
        String affiliationCertificateUrl
) {
}
