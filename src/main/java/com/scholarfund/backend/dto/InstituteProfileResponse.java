package com.scholarfund.backend.dto;

public record InstituteProfileResponse(
        Long profileId,
        String email,
        String officerName,
        String officerPhoneNo,
        boolean isVerifyByGovt,
        String instituteName,
        String collegeCode,
        String universityAffiliation,
        String principalName,
        String address,
        String affiliationCertificateUrl
) {
}
