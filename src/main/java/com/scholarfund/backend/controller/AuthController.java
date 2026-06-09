package com.scholarfund.backend.controller;

import com.scholarfund.backend.common.model.ApiResponse;
import com.scholarfund.backend.dto.AuthRequests.*;
import com.scholarfund.backend.dto.AuthResponse;
import com.scholarfund.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-student")
    public ResponseEntity<ApiResponse<String>> registerStudent(@RequestBody RegisterStudentDto request) {
        authService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(201, "CREATED", "Student registered successfully. OTP sent to email.")
        );
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<String>> requestOtp(@RequestBody RequestOtpDto request) {
        authService.requestOtp(request);
        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "OTP generated and sent to email.")
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody VerifyOtpDto request) {
        AuthResponse authData = authService.verifyOtpAndLogin(request);

        log.info("Successfully verified OTP and generated tokens for: {}", request.email());
        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "Login successful", authData)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenDto request) {
        AuthResponse newAuthData = authService.refreshAccessToken(request);
        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "Token refreshed successfully", newAuthData)
        );
    }
}