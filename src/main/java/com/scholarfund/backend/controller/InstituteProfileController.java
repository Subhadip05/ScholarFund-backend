package com.scholarfund.backend.controller;

import com.scholarfund.backend.common.model.ApiResponse;
import com.scholarfund.backend.dto.InstituteProfileDto;
import com.scholarfund.backend.dto.InstituteProfileResponse;
import com.scholarfund.backend.service.InstituteProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institute/profile")
@RequiredArgsConstructor
@Slf4j
public class InstituteProfileController {

    private final InstituteProfileService instituteProfileService;

    @PreAuthorize("hasRole('COLLAGE')")
    @PostMapping
    public ResponseEntity<ApiResponse<InstituteProfileResponse>> updateInstituteProfile(@RequestBody InstituteProfileDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName();

        log.info("Institute profile update initiated for institute: {}", email);
        InstituteProfileResponse updatedProfile = instituteProfileService.saveOrUpdateInstituteProfile(email, requestDto);
        log.info("Institute profile successfully saved for: {}", email);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "Institute Profile updated successfully", updatedProfile)
        );
    }

    @PreAuthorize("hasRole('COLLAGE')")
    @GetMapping
    public ResponseEntity<ApiResponse<InstituteProfileResponse>> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName();

        log.info("Fetching institute profile data for institute: {}", email);

        InstituteProfileResponse profileData = instituteProfileService.getInstituteProfile(email);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "Institute Profile fetched successfully", profileData)
        );
    }
}