package com.scholarfund.backend.controller;

import com.scholarfund.backend.common.model.ApiResponse;
import com.scholarfund.backend.dto.StudentProfileDto;
import com.scholarfund.backend.dto.StudentProfileResponse;
import com.scholarfund.backend.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@RequiredArgsConstructor
@Slf4j
public class StudentProfileController {

    private final StudentProfileService profileService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateProfile(@RequestBody StudentProfileDto requestDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName();

        log.info("Profile update initiated for student: {}", email);

        StudentProfileResponse updatedProfile = profileService.saveOrUpdateProfile(email, requestDto);

        log.info("Profile successfully saved for: {}", email);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "Student Profile updated successfully", updatedProfile)
        );
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName();

        log.info("Fetching profile data for student: {}", email);

        StudentProfileResponse profileData = profileService.getStudentProfile(email);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "Profile fetched successfully", profileData)
        );
    }
}