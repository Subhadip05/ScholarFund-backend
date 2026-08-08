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

        //First updating user details
        if (dto.fullName() != null && !dto.fullName().isBlank()) {
            user.setFullName(dto.fullName());
        }

        if (dto.phoneNumber() != null && !dto.phoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(dto.phoneNumber())) {
                throw new ScholarFundException("Phone number already registered to another user", ErrorCode.ALREADY_EXIST, null);
            }
            user.setPhoneNumber(dto.phoneNumber());
        }
        userRepository.save(user);


        //Then saving Student Details
        StudentProfile profile = profileRepository.findByUser(user).orElse(new StudentProfile());

        profile.setUser(user);
        profile.setFullName(dto.fullName());
        profile.setPhoneNumber(dto.phoneNumber());
        profile.setDateOfBirth(dto.dateOfBirth());
        profile.setGender(dto.gender());
        profile.setAddress(dto.address());
        profile.setIsWestBengalResident(dto.isWestBengalResident());

        if (dto.aadhaarNumber() != null && !dto.aadhaarNumber().equals(profile.getAadhaarNumber())) {
            if (profileRepository.existsByAadhaarNumber(dto.aadhaarNumber())) {
                throw new ScholarFundException("Aadhaar already registered to another account", ErrorCode.ALREADY_EXIST, null);
            }
            profile.setAadhaarNumber(dto.aadhaarNumber());
        }

        if (dto.aadhaarFileId() != null) {
            DocumentFile aadhaarDoc = documentFileRepository.findById(dto.aadhaarFileId())
                    .orElseThrow(() -> new ScholarFundException("Aadhaar reference not found.", ErrorCode.FILE_NOT_FOUND, null));
            profile.setAadhaarDocumentKey(aadhaarDoc.getS3Key());
        }

        if (dto.selfImageFileId() != null) {
            DocumentFile selfImageDoc = documentFileRepository.findById(dto.selfImageFileId())
                    .orElseThrow(() -> new ScholarFundException("Self Image reference not found.", ErrorCode.FILE_NOT_FOUND, null));
            profile.setSelfImageKey(selfImageDoc.getS3Key());
        }

        if (dto.selfSignatureFileId() != null) {
            DocumentFile selfSignatureDoc = documentFileRepository.findById(dto.selfSignatureFileId())
                    .orElseThrow(() -> new ScholarFundException("Self Signature reference not found.", ErrorCode.FILE_NOT_FOUND, null));
            profile.setSelfSignatureKey(selfSignatureDoc.getS3Key());
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

        String selfImageUrl = null;
        if (profile.getSelfImageKey() != null) {
            selfImageUrl = s3Service.generatePresignedUrl(profile.getSelfImageKey());
        }

        String selfSignatureUrl = null;
        if (profile.getSelfSignatureKey() != null) {
            selfSignatureUrl = s3Service.generatePresignedUrl(profile.getSelfSignatureKey());
        }

        return new StudentProfileResponse(
                profile.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getAddress(),
                profile.getAadhaarNumber(),
                profile.getIsWestBengalResident(),
                profile.getIsApplicationSubmit(),
                aadhaarUrl,
                selfImageUrl,
                selfSignatureUrl
        );
    }
}