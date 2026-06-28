package com.scholarfund.backend.service;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.dto.ApplicationResponse;
import com.scholarfund.backend.dto.ApplicationSubmitRequest;
import com.scholarfund.backend.entity.*;
import com.scholarfund.backend.repository.InstituteProfileRepository;
import com.scholarfund.backend.repository.ScholarshipApplicationRepository;
import com.scholarfund.backend.repository.StudentProfileRepository;
import com.scholarfund.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScholarshipApplicationService {

    private final ScholarshipApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final InstituteProfileRepository instituteProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApplicationResponse submitApplication(String email, ApplicationSubmitRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        StudentProfile studentProfile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ScholarFundException("You must complete your profile before applying.", ErrorCode.BAD_REQUEST, null));

        if (applicationRepository.existsByStudentProfileAndAcademicYear(studentProfile, request.academicYear())) {
            throw new ScholarFundException("You have already applied for a scholarship for the " + request.academicYear() + " academic year.", ErrorCode.ALREADY_EXIST, null);
        }

        InstituteProfile instituteProfile = instituteProfileRepository.findById(request.instituteId())
                .orElseThrow(() -> new ScholarFundException("Selected Institute not found.", ErrorCode.NOT_FOUND, null));

        ScholarshipApplication application = ScholarshipApplication.builder()
                .studentProfile(studentProfile)
                .instituteProfile(instituteProfile)
                .courseName(request.courseName())
                .academicYear(request.academicYear())
                .studentRemarks(request.studentRemarks())
                .status(ApplicationStatus.SUBMITTED) // Initial workflow status -- submitted
                .build();

        ScholarshipApplication savedApplication = applicationRepository.save(application);
        log.info("Scholarship application submitted successfully for student: {}", email);

        return new ApplicationResponse(
                savedApplication.getId(),
                user.getFullName(),
                instituteProfile.getInstituteName(),
                savedApplication.getCourseName(),
                savedApplication.getAcademicYear(),
                savedApplication.getStatus(),
                savedApplication.getStudentRemarks(),
                savedApplication.getInstituteRemarks(),
                savedApplication.getGovtRemarks()
        );
    }
}