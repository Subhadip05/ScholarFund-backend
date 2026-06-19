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
    private final DocumentFileRepository documentFileRepository; // Replaced S3Service with this

    @Transactional
    public StudentProfileResponse saveOrUpdateProfile(String email, StudentProfileDto dto) {

        // 1. Fetch the logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        // 2. Fetch existing profile, or create a new one
        StudentProfile profile = profileRepository.findByUser(user).orElse(new StudentProfile());

        profile.setUser(user);
        profile.setDateOfBirth(dto.dateOfBirth());
        profile.setGender(dto.gender());
        profile.setAddress(dto.address());

        // 3. Validate Aadhaar uniqueness
        if (dto.aadhaarNumber() != null && !dto.aadhaarNumber().equals(profile.getAadhaarNumber())) {
            if (profileRepository.existsByAadhaarNumber(dto.aadhaarNumber())) {
                throw new ScholarFundException("Aadhaar already registered to another account", ErrorCode.ALREADY_EXIST, null);
            }
            profile.setAadhaarNumber(dto.aadhaarNumber());
        }

        // 4. Link Aadhaar Document from the DocumentFile table
        if (dto.aadhaarFileId() != null) {
            DocumentFile aadhaarDoc = documentFileRepository.findById(dto.aadhaarFileId())
                    .orElseThrow(() -> new ScholarFundException("Aadhaar document reference not found. Please upload again.", ErrorCode.NOT_FOUND, null));
            profile.setAadhaarDocumentKey(aadhaarDoc.getS3Key());
        }

        // 5. Link Income Certificate from the DocumentFile table
        if (dto.incomeFileId() != null) {
            DocumentFile incomeDoc = documentFileRepository.findById(dto.incomeFileId())
                    .orElseThrow(() -> new ScholarFundException("Income certificate reference not found. Please upload again.", ErrorCode.NOT_FOUND, null));
            profile.setIncomeCertificateKey(incomeDoc.getS3Key());
        }

        StudentProfile savedProfile = profileRepository.save(profile);

        log.info("Student profile cleanly saved in database for user: {}", email);

        // 7. Return the cleanly mapped Response DTO
        return new StudentProfileResponse(
                savedProfile.getId(),
                user.getFullName(),
                user.getEmail(),
                savedProfile.getDateOfBirth(),
                savedProfile.getGender(),
                savedProfile.getAddress(),
                savedProfile.getAadhaarNumber()
        );
    }
}