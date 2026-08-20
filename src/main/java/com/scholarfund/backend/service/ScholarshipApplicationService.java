package com.scholarfund.backend.service;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.dto.ApplicationHistoryDto;
import com.scholarfund.backend.dto.ApplicationResponse;
import com.scholarfund.backend.dto.ApplicationStatusUpdateRequest;
import com.scholarfund.backend.dto.ApplicationSubmitRequest;
import com.scholarfund.backend.entity.*;
import com.scholarfund.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScholarshipApplicationService {

    private final ScholarshipApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final InstituteProfileRepository instituteProfileRepository;
    private final UserRepository userRepository;
    private final DocumentFileRepository documentFileRepository;
    private final S3Service s3Service;

    @Transactional
    public ApplicationResponse submitApplication(String email, ApplicationSubmitRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        StudentProfile studentProfile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ScholarFundException("You must complete your profile before applying.", ErrorCode.BAD_REQUEST, null));

        // Scholarship eligibility for student ----
        if (studentProfile.getIsWestBengalResident() == null || !studentProfile.getIsWestBengalResident()) {
            throw new ScholarFundException("Eligibility Failed: You must be a resident of West Bengal.", ErrorCode.BAD_REQUEST, null);
        }

        if (request.lastQualificationMarks() == null || request.lastQualificationMarks() < 70.0) {
            throw new ScholarFundException("Eligibility Failed: A minimum of 70% in your last qualification is required.", ErrorCode.BAD_REQUEST, null);
        }

        if (request.annualIncome() == null || request.annualIncome() >= 200000.0) {
            throw new ScholarFundException("Eligibility Failed: Family annual income must be less than 2,00,000.", ErrorCode.BAD_REQUEST, null);
        }

        if (applicationRepository.existsByStudentProfileAndAcademicYear(studentProfile, request.academicYear())) {
            throw new ScholarFundException("You have already applied for a scholarship for the " + request.academicYear() + " academic year.", ErrorCode.ALREADY_EXIST, null);
        }

        InstituteProfile instituteProfile = instituteProfileRepository.findById(request.instituteId())
                .orElseThrow(() -> new ScholarFundException("Selected Institute not found.", ErrorCode.NOT_FOUND, null));

        if (instituteProfile.getVerifiedByGovt() == null || !instituteProfile.getVerifiedByGovt()) {
            throw new ScholarFundException("Cannot apply: This institute has not yet been verified by the Government.", ErrorCode.BAD_REQUEST, null);
        }


        if (request.incomeFileId() == null) {
            throw new ScholarFundException("Income certificate file is required.", ErrorCode.BAD_REQUEST, null);
        }
        DocumentFile incomeDoc = documentFileRepository.findById(request.incomeFileId())
                .orElseThrow(() -> new ScholarFundException("Income certificate reference not found.", ErrorCode.FILE_NOT_FOUND, null));


        if(request.hsMarksheetFileId() == null){
            throw new ScholarFundException("10+2 Marksheet file is required.", ErrorCode.BAD_REQUEST, null);
        }
        DocumentFile hsMarkSheetDoc = documentFileRepository.findById(request.hsMarksheetFileId())
                .orElseThrow(() ->  new ScholarFundException("10+2 Marksheet reference not found.", ErrorCode.FILE_NOT_FOUND, null));


        if(request.bankPassbookFileId() == null){
            throw new ScholarFundException("Bank Passbook file is required.", ErrorCode.BAD_REQUEST, null);
        }
        DocumentFile bankPassbookDoc = documentFileRepository.findById(request.bankPassbookFileId())
                .orElseThrow(() ->  new ScholarFundException("Bank Passbook reference not found.", ErrorCode.FILE_NOT_FOUND, null));

        if(request.admissionReceiptFileId() == null){
            throw new ScholarFundException("Admission Receipt file is required.", ErrorCode.BAD_REQUEST, null);
        }
        DocumentFile admissionReceiptDoc = documentFileRepository.findById(request.admissionReceiptFileId())
                .orElseThrow(() ->  new ScholarFundException("Admission Receipt reference not found.", ErrorCode.FILE_NOT_FOUND, null));


        ScholarshipApplication application = ScholarshipApplication.builder()
                .studentProfile(studentProfile)
                .instituteProfile(instituteProfile)
                .applicationNumber(generateApplicationNumber(instituteProfile.getInstituteName()))
                .courseName(request.courseName())
                .academicYear(request.academicYear())
                .lastQualificationMarks(request.lastQualificationMarks())
                .lastQualificationCourse(request.lastQualificationCourse())
                .lastQualificationExamRollNo(request.lastQualificationExamRollNo())
                .passOutBoardName(request.passOutBoardName())
                .annualIncome(request.annualIncome())
                .bankAccountNumber(request.bankAccountNumber())
                .ifscCode(request.ifscCode())
                .incomeCertificateKey(incomeDoc.getS3Key())
                .hsMarksheetKey(hsMarkSheetDoc.getS3Key())
                .bankPassbookKey(bankPassbookDoc.getS3Key())
                .admissionReceiptKey(admissionReceiptDoc.getS3Key())
                .status(ApplicationStatus.SUBMITTED)
                .build();

        ApplicationHistory initialHistory = ApplicationHistory.builder()
                .application(application)
                .actionTaken("Application Submitted")
                .actionBy(studentProfile.getFullName())
                .actorRole("STUDENT")
                .remarks("Student submitted the application successfully.")
                .actionTime(LocalDateTime.now())
                .build();

        application.getTrackRecords().add(initialHistory);

        ScholarshipApplication savedApplication = applicationRepository.save(application);
        log.info("Scholarship application submitted successfully for student: {}", email);

        studentProfile.setIsApplicationSubmit(true);
        studentProfileRepository.save(studentProfile);
        log.info("Scholarship application submitted by : {}", studentProfile.getFullName());

        return applicationMapToResponse(savedApplication);
    }

    public String generateApplicationNumber(String instituteName) {
        String prefix = (instituteName != null && instituteName.trim().length() >= 2)
                ? instituteName.trim().substring(0, 2).toUpperCase()
                : "SC";

        int year = LocalDate.now().getYear();
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        return String.format("%s-%d-%s", prefix, year, suffix);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByInstituteId(Long instituteProfileId) {
        if (!instituteProfileRepository.existsById(instituteProfileId)) {
            throw new ScholarFundException("Institute profile not found.", ErrorCode.NOT_FOUND, null);
        }

        List<ScholarshipApplication> applications = applicationRepository.findByInstituteProfileId(instituteProfileId);

        return applications.stream()
                .map(this::applicationMapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByStudentId(Long studentProfileId) {
        if (!studentProfileRepository.existsById(studentProfileId)) {
            throw new ScholarFundException("Student profile not found.", ErrorCode.NOT_FOUND, null);
        }

        List<ScholarshipApplication> applications = applicationRepository.findByStudentProfileId(studentProfileId);

        return applications.stream()
                .map(this::applicationMapToResponse)
                .collect(Collectors.toList());
    }

    // Helper method to dynamically generate the 15-minute secure AWS URLs
    public ApplicationResponse applicationMapToResponse(ScholarshipApplication savedApplication) {

        String incomeUrl = null;
        if (savedApplication.getIncomeCertificateKey() != null) {
            incomeUrl = s3Service.generatePresignedUrl(savedApplication.getIncomeCertificateKey());
        }

        String hsMarksheetUrl = null;
        if (savedApplication.getHsMarksheetKey() != null) {
            hsMarksheetUrl = s3Service.generatePresignedUrl(savedApplication.getHsMarksheetKey());
        }

        String bankPassbookUrl = null;
        if (savedApplication.getBankPassbookKey() != null) {
            bankPassbookUrl = s3Service.generatePresignedUrl(savedApplication.getBankPassbookKey());
        }

        String admissionReceiptUrl = null;
        if (savedApplication.getAdmissionReceiptKey() != null) {
            admissionReceiptUrl = s3Service.generatePresignedUrl(savedApplication.getAdmissionReceiptKey());
        }

        // Map the track records into our DTO for the frontend
        List<ApplicationHistoryDto> timeline = savedApplication.getTrackRecords().stream()
                .map(history -> new ApplicationHistoryDto(
                        history.getActionTaken(),
                        history.getActionBy(),
                        history.getActorRole(),
                        history.getRemarks(),
                        history.getActionTime()
                ))
                .collect(Collectors.toList());

        return new ApplicationResponse(
                savedApplication.getId(),
                savedApplication.getStudentProfile().getFullName(),
                savedApplication.getInstituteProfile().getInstituteName(),
                savedApplication.getCourseName(),
                savedApplication.getAcademicYear(),
                savedApplication.getLastQualificationMarks(),
                savedApplication.getLastQualificationCourse(),
                savedApplication.getLastQualificationExamRollNo(),
                savedApplication.getPassOutBoardName(),
                savedApplication.getAnnualIncome(),
                savedApplication.getBankAccountNumber(),
                savedApplication.getIfscCode(),
                savedApplication.getStatus(),
                incomeUrl,
                hsMarksheetUrl,
                bankPassbookUrl,
                admissionReceiptUrl,
                timeline
        );
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId,
                                                       ApplicationStatusUpdateRequest request,
                                                       String actorEmail) {

        ScholarshipApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ScholarFundException("Application not found", ErrorCode.NOT_FOUND, null));

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        String actorRole = actor.getRole().getRoleName().toUpperCase();
        String actionByName = resolveActorName(actor, actorRole);

        application.setStatus(request.statusAction());

        ApplicationHistory statusUpdateHistory = ApplicationHistory.builder()
                .application(application)
                .actionTaken("Status updated to " + request.statusAction().name())
                .actionBy(actionByName)
                .actorRole(actorRole)
                .remarks(request.actionRemarks())
                .actionTime(LocalDateTime.now())
                .build();

        application.getTrackRecords().add(statusUpdateHistory);
        ScholarshipApplication savedApplication = applicationRepository.save(application);

        log.info("Application ID {} status updated to {} by {}", applicationId, request.statusAction(), actorEmail);

        return applicationMapToResponse(savedApplication);
    }

    private String resolveActorName(User actor, String roleName) {
        if ("INSTITUTE".equals(roleName)) {
            return instituteProfileRepository.findByUser(actor)
                    .map(InstituteProfile::getInstituteName)
                    .orElse(actor.getEmail());
        } else if ("GOVT".equals(roleName)) {
            return "Government Administrator";
        }
        return actor.getEmail();
    }
}