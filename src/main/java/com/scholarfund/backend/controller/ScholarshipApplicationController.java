package com.scholarfund.backend.controller;

import com.scholarfund.backend.common.model.ApiResponse;
import com.scholarfund.backend.dto.ApplicationResponse;
import com.scholarfund.backend.dto.ApplicationStatusUpdateRequest;
import com.scholarfund.backend.dto.ApplicationSubmitRequest;
import com.scholarfund.backend.service.ScholarshipApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PreAuthorize("hasAnyRole('INSTITUTE')")
    @GetMapping("/institute/{instituteProfileId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplicationsByInstituteId(
            @PathVariable Long instituteProfileId) {

        log.info("Fetching scholarship applications for institute profile ID: {}", instituteProfileId);

        List<ApplicationResponse> applications = applicationService.getApplicationsByInstituteId(instituteProfileId);

        String message = applications.isEmpty()
                ? "No applications found for this institute."
                : "Applications fetched successfully.";

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", message, applications)
        );
    }

    @GetMapping("/student/{studentProfileId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplicationsByStudentId(
            @PathVariable Long studentProfileId) {

        log.info("Fetching scholarship applications for student profile ID: {}", studentProfileId);

        List<ApplicationResponse> applications = applicationService.getApplicationsByStudentId(studentProfileId);

        String message = applications.isEmpty()
                ? "No applications found for this student."
                : "Student applications fetched successfully.";

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", message, applications)
        );
    }

    @PreAuthorize("hasAnyRole('INSTITUTE', 'GOVT')")
    @PutMapping("/{applicationId}/update-status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} is updating application ID {} to status {}",
                userDetails.getUsername(), applicationId, request.statusAction());

        ApplicationResponse updatedApplication = applicationService.updateApplicationStatus(
                applicationId, request, userDetails.getUsername()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "Application status updated successfully.", updatedApplication)
        );
    }
}