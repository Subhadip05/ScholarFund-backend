package com.scholarfund.backend.entity;

public enum ApplicationStatus {
    SUBMITTED,          // 1. Student applied, waiting for Institute to review
    INSTITUTE_VERIFIED, // 2. Institute approved, waiting for Govt (Admin)
    INSTITUTE_REJECTED, // 3. Institute rejected it (fake student, wrong fees, etc.)
    ADMIN_APPROVED,     // 4. Govt approved the scholarship
    ADMIN_REJECTED,     // 5. Govt rejected the scholarship
    DISBURSED           // 6. Money has been credited to the student's bank account
}