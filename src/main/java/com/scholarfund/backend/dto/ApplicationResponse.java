package com.scholarfund.backend.dto;

import com.scholarfund.backend.entity.ApplicationStatus;

public record ApplicationResponse(
        Long applicationId,
        String studentName,
        String instituteName,
        String courseName,
        String academicYear,
        ApplicationStatus status,

        String studentRemarks,
        String instituteRemarks,
        String govtRemarks
) {}