package com.scholarfund.backend.dto;

public record InstituteProfileDto(
        String instituteName,
        String collegeCode,
        String universityAffiliation,
        String principalName,
        String officerPhoneNo,
        String officerName,
        String address,
        Long affiliationCertificateFileId
) {}
