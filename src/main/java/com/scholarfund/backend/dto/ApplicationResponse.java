package com.scholarfund.backend.dto;

import com.scholarfund.backend.entity.ApplicationStatus;
import java.util.List;

public record ApplicationResponse(
        Long applicationId,
        String studentName,
        String instituteName,
        String courseName,
        String academicYear,
        Double lastQualificationMarks,
        String lastQualificationCourse,
        String lastQualificationExamRollNo,
        String passOutBoardName,
        Double annualIncome,
        String bankAccountNumber,
        String ifscCode,
        ApplicationStatus status,

        // AWS Presigned URLs for frontend display
        String incomeCertificateUrl,
        String hsMarksheetUrl,
        String bankPassbookUrl,

        List<ApplicationHistoryDto> timeline
) {}