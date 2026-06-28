package com.scholarfund.backend.dto;

public record ApplicationSubmitRequest(
        Long instituteId,        // The ID of the college they selected from the dropdown
        String courseName,       // e.g., "B.Tech Computer Science"
        String academicYear,     // e.g., "2026-2027"
        String studentRemarks    // Any note the student wants to add
) {}