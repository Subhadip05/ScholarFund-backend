package com.scholarfund.backend.controller;

import com.scholarfund.backend.common.model.ApiResponse;
import com.scholarfund.backend.dto.ApplicationResponse;
import com.scholarfund.backend.dto.ApplicationSubmitRequest;
import com.scholarfund.backend.service.ScholarshipApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ScholarshipApplicationController {

    private final ScholarshipApplicationService applicationService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<ApplicationResponse>> submitApplication(@RequestBody ApplicationSubmitRequest requestDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName();

        log.info("Scholarship application submission initiated by: {}", email);

        ApplicationResponse response = applicationService.submitApplication(email, requestDto);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "Scholarship application submitted successfully", response)
        );
    }
}