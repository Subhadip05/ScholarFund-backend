package com.scholarfund.backend.controller;

import com.scholarfund.backend.common.model.ApiResponse;
import com.scholarfund.backend.dto.InstituteProfileResponse;
import com.scholarfund.backend.service.InstituteProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final InstituteProfileService instituteProfileService;

    @PreAuthorize("hasRole('GOVT')")
    @GetMapping("/institutes/unverified-list")
    public ResponseEntity<ApiResponse<List<InstituteProfileResponse>>> getUnverifiedInstitutes() {
        log.info("Admin fetching all unverified institutes");

        List<InstituteProfileResponse> unverifiedList = instituteProfileService.getUnverifiedInstitutes();

        String message = unverifiedList.isEmpty()
                ? "There is no unverified college"
                : "Unverified institutes fetched successfully";

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", message, unverifiedList,unverifiedList.size())
        );
    }


    @PreAuthorize("hasRole('GOVT')")
    @PutMapping("/verify-institute/{instituteId}")
    public ResponseEntity<ApiResponse<String>> verifyInstitute(@PathVariable Long instituteId) {
        log.info("Admin verifying institute with ID: {}", instituteId);

        String result = instituteProfileService.verifyInstitute(instituteId);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", result, null)
        );
    }
}