package com.scholarfund.backend.service;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.dto.AuthRequests.*;
import com.scholarfund.backend.dto.AuthResponse;
import com.scholarfund.backend.entity.OtpVerification;
import com.scholarfund.backend.entity.RefreshToken;
import com.scholarfund.backend.entity.Role;
import com.scholarfund.backend.entity.User;
import com.scholarfund.backend.repository.OtpVerificationRepository;
import com.scholarfund.backend.repository.RefreshTokenRepository;
import com.scholarfund.backend.repository.RoleRepository;
import com.scholarfund.backend.repository.UserRepository;
import com.scholarfund.backend.security.JwtUtil;
import com.scholarfund.backend.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpVerificationRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Transactional
    public void registerStudent(RegisterStudentDto dto) {
        Optional<User> existingUserOpt = userRepository.findByEmail(dto.email());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // If they are already fully verified, throw the error
            if (existingUser.getIsVerified()) {
                throw new ScholarFundException("User already registered and verified. Please login.", ErrorCode.ALREADY_EXIST, null);
            } else {
                // If they exist but never finished OTP verification, just resend the OTP and stop here.
                log.info("Unverified user {} attempting to register again. Resending OTP.", dto.email());
                requestOtp(new RequestOtpDto(dto.email()));
                return;
            }
        }

        // 2. Check phone number only for completely new registrations
        if (userRepository.existsByPhoneNumber(dto.phoneNumber())) {
            throw new ScholarFundException("Phone number already in use", ErrorCode.ALREADY_EXIST, null);
        }

        // 3. Proceed with new user creation
        Role studentRole = roleRepository.findByRoleName("STUDENT")
                .orElseThrow(() -> new ScholarFundException("Role not found", ErrorCode.NOT_FOUND, null));

        User user = new User();
        user.setEmail(dto.email());
        user.setFullName(dto.fullName());
        user.setPhoneNumber(dto.phoneNumber());
        user.setRole(studentRole);
        user.setIsVerified(false); // Stays false until first successful OTP verification
        user.setIsActive(true);

        userRepository.save(user);

        log.info("Created new unverified student record for {}", dto.email());
        requestOtp(new RequestOtpDto(dto.email()));
    }

    @Transactional
    public void requestOtp(RequestOtpDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // Fix the race condition: Check for an existing OTP row first
        Optional<OtpVerification> existingOtpOpt = otpRepository.findByEmail(user.getEmail());
        OtpVerification otpVerification;

        if (existingOtpOpt.isPresent()) {
            // If they already have an old OTP row, just update the code and reset the timer
            otpVerification = existingOtpOpt.get();
            otpVerification.setOtpCode(otpCode);
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            otpVerification.setAttempts(0); // Reset failed attempts
        } else {
            // If this is their very first time requesting an OTP, build a new row
            otpVerification = OtpVerification.builder()
                    .email(user.getEmail())
                    .otpCode(otpCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .attempts(0)
                    .build();
        }

        // Save will automatically execute an UPDATE or an INSERT cleanly
        otpRepository.save(otpVerification);

        emailService.sendOtpEmail(user.getEmail(), otpCode);

        log.info("OTP generation complete for {}", user.getEmail());
    }

    @Transactional
    public AuthResponse verifyOtpAndLogin(VerifyOtpDto dto) {
        OtpVerification otpRecord = otpRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ScholarFundException("No pending OTP found", ErrorCode.NOT_FOUND, null));

        if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpRecord);
            throw new ScholarFundException("OTP expired. Please request a new one.", ErrorCode.INVALID_OTP, null);
        }

        if (!otpRecord.getOtpCode().equals(dto.otpCode())) {
            otpRecord.setAttempts(otpRecord.getAttempts() + 1);
            otpRepository.save(otpRecord);
            throw new ScholarFundException("Invalid OTP", ErrorCode.INVALID_OTP, null);
        }

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        // Mark user as verified upon first successful login
        if (!user.getIsVerified()) {
            user.setIsVerified(true);
            userRepository.save(user);
        }

        // Clean up used OTP
        otpRepository.delete(otpRecord);

        return generateTokens(user);
    }

    @Transactional
    public AuthResponse refreshAccessToken(RefreshTokenDto dto) {
        RefreshToken tokenRecord = refreshTokenRepository.findByToken(dto.refreshToken())
                .orElseThrow(() -> new ScholarFundException("Invalid refresh token", ErrorCode.INVALID_TOKEN, null));

        if (tokenRecord.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(tokenRecord);
            throw new ScholarFundException("Refresh token expired. Please login again.", ErrorCode.EXPIRED_REFRESH_TOKEN, null);
        }

        return generateTokens(tokenRecord.getUser());
    }

    private AuthResponse generateTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenString = UUID.randomUUID().toString() + "-" + jwtUtil.generateRefreshToken(user);

        // Check if the user already has a token row in the database
        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUser(user);
        RefreshToken refreshToken;

        if (existingTokenOpt.isPresent()) {
            // If it exists, just update the token string and expiry date (No INSERT required)
            refreshToken = existingTokenOpt.get();
            refreshToken.setToken(refreshTokenString);
            refreshToken.setExpiryDate(LocalDateTime.now().plusDays(30));
        } else {
            // If it is their first time logging in, build a brand-new row
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(refreshTokenString)
                    .expiryDate(LocalDateTime.now().plusDays(30))
                    .build();
        }

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenString,
                user.getRole().getRoleName(),
                user.getFullName(),
                user.getEmail()
        );
    }
}