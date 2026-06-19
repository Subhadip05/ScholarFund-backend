package com.scholarfund.backend.dto;

import java.time.LocalDate;

public record StudentProfileDto(
        LocalDate dateOfBirth,
        String gender,
        String address,
        String aadhaarNumber,

        Long aadhaarFileId,
        Long incomeFileId
) {}