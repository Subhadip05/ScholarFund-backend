package com.scholarfund.backend.dto;

public record ApplicationSubmitRequest(
        Long instituteId,
        String courseName,
        String academicYear,
        Double lastQualificationMarks,
        String lastQualificationCourse,
        String lastQualificationExamRollNo,
        String passOutBoardName,
        Double annualIncome,
        String bankAccountNumber,
        String ifscCode,

        Long incomeFileId,
        Long hsMarksheetFileId,
        Long bankPassbookFileId
) {}