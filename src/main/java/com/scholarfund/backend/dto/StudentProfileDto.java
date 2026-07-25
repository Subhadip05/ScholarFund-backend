package com.scholarfund.backend.dto;

import java.time.LocalDate;

public record StudentProfileDto(
        String fullName,
        String phoneNumber,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String aadhaarNumber,
        Boolean isWestBengalResident,

        Long aadhaarFileId,
        Long selfImageFileId,
        Long selfSignatureFileId
) {}