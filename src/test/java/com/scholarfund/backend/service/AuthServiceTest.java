package com.scholarfund.backend.service;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.dto.AuthRequests;
import com.scholarfund.backend.dto.AuthRequests.RegisterStudentDto;
import com.scholarfund.backend.dto.AuthRequests.RequestOtpDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OtpVerificationRepository otpRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private Role studentRole;
    private User studentUser;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setRoleName("STUDENT");

        studentUser = new User();
        studentUser.setId(1L);
        studentUser.setEmail("rahul.sharma@example.com");
        studentUser.setFullName("Rahul Sharma");
        studentUser.setPhoneNumber("9876543210");
        studentUser.setRole(studentRole);
        studentUser.setIsVerified(false);
        studentUser.setIsActive(true);
    }

    // registerStudent() Test Cases
    @Nested
    @DisplayName("Tests for registerStudent")
    class RegisterStudentTests {

        @Test
        @DisplayName("Should successfully register a new student and trigger OTP email")
        void registerStudent_Success() {
            RegisterStudentDto dto = new RegisterStudentDto("rahul.sharma@example.com", "Rahul Sharma", "9876543210");

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            when(userRepository.existsByPhoneNumber(dto.phoneNumber())).thenReturn(false);
            when(roleRepository.findByRoleName("STUDENT")).thenReturn(Optional.of(studentRole));

            // requestOtp() is invoked internally upon registration
            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty(), Optional.of(studentUser));
            when(otpRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

            authService.registerStudent(dto);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo("rahul.sharma@example.com");
            assertThat(capturedUser.getFullName()).isEqualTo("Rahul Sharma");
            assertThat(capturedUser.getPhoneNumber()).isEqualTo("9876543210");
            assertThat(capturedUser.getIsVerified()).isFalse();
            assertThat(capturedUser.getRole().getRoleName()).isEqualTo("STUDENT");

            verify(otpRepository, times(1)).save(any(OtpVerification.class));
            verify(emailService, times(1)).sendOtpEmail(eq("rahul.sharma@example.com"), anyString());
        }

        @Test
        @DisplayName("Should throw ALREADY_EXIST exception if user is already verified")
        void registerStudent_ThrowsWhenUserAlreadyVerified() {
            RegisterStudentDto dto = new RegisterStudentDto("rahul.sharma@example.com", "Rahul Sharma", "9876543210");
            studentUser.setIsVerified(true);

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(studentUser));

            assertThatThrownBy(() -> authService.registerStudent(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("User already registered and verified")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.ALREADY_EXIST));

            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendOtpEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("Should resend OTP without creating duplicate user if user exists but is unverified")
        void registerStudent_ResendsOtpForUnverifiedUser() {
            RegisterStudentDto dto = new RegisterStudentDto("rahul.sharma@example.com", "Rahul Sharma", "9876543210");
            studentUser.setIsVerified(false);

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(studentUser));
            when(otpRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

            authService.registerStudent(dto);

            verify(userRepository, never()).save(any(User.class));
            verify(otpRepository, times(1)).save(any(OtpVerification.class));
            verify(emailService, times(1)).sendOtpEmail(eq("rahul.sharma@example.com"), anyString());
        }

        @Test
        @DisplayName("Should throw ALREADY_EXIST exception if phone number is already registered")
        void registerStudent_ThrowsWhenPhoneAlreadyInUse() {
            RegisterStudentDto dto = new RegisterStudentDto("rahul.sharma@example.com", "Rahul Sharma", "9876543210");

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            when(userRepository.existsByPhoneNumber(dto.phoneNumber())).thenReturn(true);

            assertThatThrownBy(() -> authService.registerStudent(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("Phone number already in use")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.ALREADY_EXIST));

            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendOtpEmail(anyString(), anyString());
        }
    }

    // requestOtp() Test Cases
    @Nested
    @DisplayName("Tests for requestOtp")
    class RequestOtpTests {

        @Test
        @DisplayName("Should generate OTP, save verification record, and trigger email send")
        void requestOtp_Success() {
            RequestOtpDto dto = new RequestOtpDto("rahul.sharma@example.com", "STUDENT");

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(studentUser));
            when(otpRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

            authService.requestOtp(dto);

            ArgumentCaptor<OtpVerification> otpCaptor = ArgumentCaptor.forClass(OtpVerification.class);
            verify(otpRepository, times(1)).save(otpCaptor.capture());

            OtpVerification savedOtp = otpCaptor.getValue();
            assertThat(savedOtp.getEmail()).isEqualTo("rahul.sharma@example.com");
            assertThat(savedOtp.getOtpCode()).matches("\\d{6}");
            assertThat(savedOtp.getAttempts()).isZero();
            assertThat(savedOtp.getExpiresAt()).isAfter(LocalDateTime.now());

            verify(emailService, times(1)).sendOtpEmail(eq("rahul.sharma@example.com"), eq(savedOtp.getOtpCode()));
        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND exception when email does not exist")
        void requestOtp_ThrowsWhenUserNotFound() {
            RequestOtpDto dto = new RequestOtpDto("unknown@example.com", "STUDENT");

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.requestOtp(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("User not found")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(otpRepository, never()).save(any(OtpVerification.class));
            verify(emailService, never()).sendOtpEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw USER_EXIST_DIFF_ROLE exception if user role does not match requested portal role")
        void requestOtp_ThrowsWhenRoleMismatch() {
            RequestOtpDto dto = new RequestOtpDto("rahul.sharma@example.com", "COLLEGE");

            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(studentUser));

            assertThatThrownBy(() -> authService.requestOtp(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("STUDENT portal access")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_EXIST_DIFF_ROLE));

            verify(otpRepository, never()).save(any(OtpVerification.class));
            verify(emailService, never()).sendOtpEmail(anyString(), anyString());
        }
    }

    // verifyOtpAndLogin() Test Cases
    @Nested
    @DisplayName("Tests for verifyOtpAndLogin")
    class VerifyOtpAndLoginTests {

        private OtpVerification activeOtp;

        @BeforeEach
        void initOtp() {
            activeOtp = OtpVerification.builder()
                    .email("rahul.sharma@example.com")
                    .otpCode("123456")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .attempts(0)
                    .build();
        }

        @Test
        @DisplayName("Should successfully verify OTP, mark user as verified, delete OTP, and return tokens")
        void verifyOtpAndLogin_Success() {
            AuthRequests.VerifyOtpDto dto = new AuthRequests.VerifyOtpDto("rahul.sharma@example.com", "123456", "STUDENT");

            when(otpRepository.findByEmail(dto.email())).thenReturn(Optional.of(activeOtp));
            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(studentUser));
            when(jwtUtil.generateAccessToken(studentUser)).thenReturn("mock-access-token");
            when(jwtUtil.generateRefreshToken(studentUser)).thenReturn("mock-refresh-token");
            when(refreshTokenRepository.findByUser(studentUser)).thenReturn(Optional.empty());

            AuthResponse response = authService.verifyOtpAndLogin(dto);

            // 1. Verify User verification state changed
            assertThat(studentUser.getIsVerified()).isTrue();
            verify(userRepository, times(1)).save(studentUser);

            // 2. Verify OTP was cleaned up
            verify(otpRepository, times(1)).delete(activeOtp);

            // 3. Verify RefreshToken persisted
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));

            // 4. Verify AuthResponse structure
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("mock-access-token");
            assertThat(response.role()).isEqualTo("STUDENT");
            assertThat(response.email()).isEqualTo("rahul.sharma@example.com");
            assertThat(response.fullName()).isEqualTo("Rahul Sharma");
            assertThat(response.refreshToken()).contains("mock-refresh-token");
        }

        @Test
        @DisplayName("Should throw NOT_FOUND exception when no OTP record exists for email")
        void verifyOtpAndLogin_ThrowsWhenNoOtpRecordFound() {
            AuthRequests.VerifyOtpDto dto = new AuthRequests.VerifyOtpDto("rahul.sharma@example.com", "123456", "STUDENT");

            when(otpRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.verifyOtpAndLogin(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("No email found for verification")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));

            verify(otpRepository, never()).delete(any());
            verify(jwtUtil, never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("Should delete expired OTP and throw INVALID_OTP exception")
        void verifyOtpAndLogin_ThrowsWhenOtpExpired() {
            AuthRequests.VerifyOtpDto dto = new AuthRequests.VerifyOtpDto("rahul.sharma@example.com", "123456", "STUDENT");
            activeOtp.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired

            when(otpRepository.findByEmail(dto.email())).thenReturn(Optional.of(activeOtp));

            assertThatThrownBy(() -> authService.verifyOtpAndLogin(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("OTP expired")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_OTP));

            // Verify expired OTP is removed from DB
            verify(otpRepository, times(1)).delete(activeOtp);
            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should increment attempts and throw INVALID_OTP exception on mismatched OTP code")
        void verifyOtpAndLogin_ThrowsWhenOtpCodeIncorrect() {
            AuthRequests.VerifyOtpDto dto = new AuthRequests.VerifyOtpDto("rahul.sharma@example.com", "999999", "STUDENT"); // Wrong code

            when(otpRepository.findByEmail(dto.email())).thenReturn(Optional.of(activeOtp));

            assertThatThrownBy(() -> authService.verifyOtpAndLogin(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("Invalid OTP")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_OTP));

            // Verify attempts incremented and saved
            assertThat(activeOtp.getAttempts()).isEqualTo(1);
            verify(otpRepository, times(1)).save(activeOtp);
            verify(otpRepository, never()).delete(any());
            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw USER_EXIST_DIFF_ROLE exception if requested role does not match user's registered role")
        void verifyOtpAndLogin_ThrowsWhenRoleMismatch() {
            AuthRequests.VerifyOtpDto dto = new AuthRequests.VerifyOtpDto("rahul.sharma@example.com", "123456", "COLLEGE"); // User is STUDENT

            when(otpRepository.findByEmail(dto.email())).thenReturn(Optional.of(activeOtp));
            when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(studentUser));

            assertThatThrownBy(() -> authService.verifyOtpAndLogin(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("Invalid role for this user")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_EXIST_DIFF_ROLE));

            verify(otpRepository, never()).delete(any());
            verify(jwtUtil, never()).generateAccessToken(any());
        }
    }

    // refreshAccessToken() Test Cases
    @Nested
    @DisplayName("Tests for refreshAccessToken")
    class RefreshAccessTokenTests {

        private RefreshToken validToken;

        @BeforeEach
        void initToken() {
            validToken = RefreshToken.builder()
                    .user(studentUser)
                    .token("existing-valid-refresh-token")
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .build();
        }

        @Test
        @DisplayName("Should successfully issue new access and refresh tokens when token is valid")
        void refreshAccessToken_Success() {
            AuthRequests.RefreshTokenDto dto = new AuthRequests.RefreshTokenDto("existing-valid-refresh-token");

            when(refreshTokenRepository.findByToken(dto.refreshToken())).thenReturn(Optional.of(validToken));
            when(jwtUtil.generateAccessToken(studentUser)).thenReturn("new-access-token");
            when(jwtUtil.generateRefreshToken(studentUser)).thenReturn("new-refresh-token");
            when(refreshTokenRepository.findByUser(studentUser)).thenReturn(Optional.of(validToken));

            AuthResponse response = authService.refreshAccessToken(dto);

            // Token entity updated in place
            verify(refreshTokenRepository, times(1)).save(validToken);
            assertThat(validToken.getToken()).contains("new-refresh-token");

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.email()).isEqualTo("rahul.sharma@example.com");
        }

        @Test
        @DisplayName("Should throw INVALID_TOKEN exception when refresh token does not exist in DB")
        void refreshAccessToken_ThrowsWhenTokenNotFound() {
            AuthRequests.RefreshTokenDto dto = new AuthRequests.RefreshTokenDto("unknown-token");

            when(refreshTokenRepository.findByToken(dto.refreshToken())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshAccessToken(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("Invalid refresh token")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));

            verify(jwtUtil, never()).generateAccessToken(any());
            verify(refreshTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should delete expired refresh token and throw EXPIRED_REFRESH_TOKEN exception")
        void refreshAccessToken_ThrowsWhenTokenExpired() {
            AuthRequests.RefreshTokenDto dto = new AuthRequests.RefreshTokenDto("expired-token");
            validToken.setExpiryDate(LocalDateTime.now().minusDays(1)); // Expired

            when(refreshTokenRepository.findByToken(dto.refreshToken())).thenReturn(Optional.of(validToken));

            assertThatThrownBy(() -> authService.refreshAccessToken(dto))
                    .isInstanceOf(ScholarFundException.class)
                    .hasMessageContaining("Refresh token expired")
                    .satisfies(ex -> assertThat(((ScholarFundException) ex).getErrorCode()).isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN));

            verify(refreshTokenRepository, times(1)).delete(validToken);
            verify(jwtUtil, never()).generateAccessToken(any());
        }
    }
}