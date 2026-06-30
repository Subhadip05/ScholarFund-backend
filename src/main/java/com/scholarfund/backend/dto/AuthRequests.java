package com.scholarfund.backend.dto;

public class AuthRequests {
    public record RequestOtpDto(String email) {}

    public record VerifyOtpDto(String email, String otpCode) {}

    public record RegisterStudentDto(String email, String fullName, String phoneNumber) {}

    public record RegisterCollegeDto(String email, String contactPersonName, String phoneNumber) {}

    public record RefreshTokenDto(String refreshToken) {}
}