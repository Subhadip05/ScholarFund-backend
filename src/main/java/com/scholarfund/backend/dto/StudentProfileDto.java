package com.scholarfund.backend.dto;

import java.time.LocalDate;

public record StudentProfileDto(
        LocalDate dateOfBirth,
        String gender,
        String address,
        String aadhaarNumber,
        Double lastQualificationMarks,
        String lastQualificationCourse,
        Double annualIncome,
        Boolean isWestBengalResident,
        String bankAccountNumber,
        String ifscCode,

        Long aadhaarFileId,
        Long incomeFileId,
        Long hsMarksheetFileId,
        Long bankPassbookFileId
) {}