package com.scholarfund.backend.service;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.dto.StudentProfileDto;
import com.scholarfund.backend.dto.StudentProfileResponse;
import com.scholarfund.backend.entity.DocumentFile;
import com.scholarfund.backend.entity.StudentProfile;
import com.scholarfund.backend.entity.User;
import com.scholarfund.backend.repository.DocumentFileRepository;
import com.scholarfund.backend.repository.StudentProfileRepository;
import com.scholarfund.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final DocumentFileRepository documentFileRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public StudentProfileResponse getStudentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        StudentProfile studentProfile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ScholarFundException("Profile not found. Please create one.", ErrorCode.NOT_FOUND, null));

        return mapToResponse(studentProfile, user);
    }

    @Transactional
    public StudentProfileResponse saveOrUpdateProfile(String email, StudentProfileDto dto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        StudentProfile profile = profileRepository.findByUser(user).orElse(new StudentProfile());

        profile.setUser(user);
        profile.setDateOfBirth(dto.dateOfBirth());
        profile.setGender(dto.gender());
        profile.setAddress(dto.address());
        profile.setAnnualIncome(dto.annualIncome());
        profile.setLastQualificationMarks(dto.lastQualificationMarks());
        profile.setLastQualificationCourse(dto.lastQualificationCourse());
        profile.setIsWestBengalResident(dto.isWestBengalResident());
        profile.setBankAccountNumber(dto.bankAccountNumber());
        profile.setIfscCode(dto.ifscCode());

        if (dto.aadhaarNumber() != null && !dto.aadhaarNumber().equals(profile.getAadhaarNumber())) {
            if (profileRepository.existsByAadhaarNumber(dto.aadhaarNumber())) {
                throw new ScholarFundException("Aadhaar already registered to another account", ErrorCode.ALREADY_EXIST, null);
            }
            profile.setAadhaarNumber(dto.aadhaarNumber());
        }

        if (dto.aadhaarFileId() != null) {
            DocumentFile aadhaarDoc = documentFileRepository.findById(dto.aadhaarFileId())
                    .orElseThrow(() -> new ScholarFundException("Aadhaar reference not found.", ErrorCode.NOT_FOUND, null));
            profile.setAadhaarDocumentKey(aadhaarDoc.getS3Key());
        }

        if (dto.incomeFileId() != null) {
            DocumentFile incomeDoc = documentFileRepository.findById(dto.incomeFileId())
                    .orElseThrow(() -> new ScholarFundException("Income certificate reference not found.", ErrorCode.NOT_FOUND, null));
            profile.setIncomeCertificateKey(incomeDoc.getS3Key());
        }

        if (dto.hsMarksheetFileId() != null) {
            DocumentFile doc = documentFileRepository.findById(dto.hsMarksheetFileId())
                    .orElseThrow(() -> new ScholarFundException("10+2 Marksheet reference not found.", ErrorCode.NOT_FOUND, null));
            profile.setHsMarksheetKey(doc.getS3Key());
        }

        if (dto.bankPassbookFileId() != null) {
            DocumentFile doc = documentFileRepository.findById(dto.bankPassbookFileId())
                    .orElseThrow(() -> new ScholarFundException("Bank Passbook reference not found.", ErrorCode.NOT_FOUND, null));
            profile.setBankPassbookKey(doc.getS3Key());
        }

        StudentProfile savedProfile = profileRepository.save(profile);
        log.info("Student profile cleanly saved in database for user: {}", email);

        return mapToResponse(savedProfile, user);
    }

    // Helper method to dynamically generate the 15-minute secure AWS URLs
    private StudentProfileResponse mapToResponse(StudentProfile profile, User user) {

        String aadhaarUrl = null;
        if (profile.getAadhaarDocumentKey() != null) {
            aadhaarUrl = s3Service.generatePresignedUrl(profile.getAadhaarDocumentKey());
        }

        String incomeUrl = null;
        if (profile.getIncomeCertificateKey() != null) {
            incomeUrl = s3Service.generatePresignedUrl(profile.getIncomeCertificateKey());
        }

        String hsMarksheetUrl = null;
        if (profile.getHsMarksheetKey() != null) {
            hsMarksheetUrl = s3Service.generatePresignedUrl(profile.getHsMarksheetKey());
        }

        String bankPassbookUrl = null;
        if (profile.getBankPassbookKey() != null) {
            bankPassbookUrl = s3Service.generatePresignedUrl(profile.getBankPassbookKey());
        }

        return new StudentProfileResponse(
                profile.getId(),
                user.getFullName(),
                user.getEmail(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getAddress(),
                profile.getAadhaarNumber(),
                profile.getLastQualificationMarks(),
                profile.getLastQualificationCourse(),
                profile.getAnnualIncome(),
                profile.getIsWestBengalResident(),
                profile.getBankAccountNumber(),
                profile.getIfscCode(),
                aadhaarUrl,
                incomeUrl,
                hsMarksheetUrl,
                bankPassbookUrl
        );
    }
}