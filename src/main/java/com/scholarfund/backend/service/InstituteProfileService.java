package com.scholarfund.backend.service;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.dto.InstituteProfileDto;
import com.scholarfund.backend.dto.InstituteProfileResponse;
import com.scholarfund.backend.entity.DocumentFile;
import com.scholarfund.backend.entity.InstituteProfile;
import com.scholarfund.backend.entity.User;
import com.scholarfund.backend.repository.DocumentFileRepository;
import com.scholarfund.backend.repository.InstituteProfileRepository;
import com.scholarfund.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstituteProfileService {

    private final InstituteProfileRepository instituteProfileRepository;
    private final UserRepository userRepository;
    private final DocumentFileRepository documentFileRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public InstituteProfileResponse getInstituteProfile(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND,null));

        InstituteProfile profile = instituteProfileRepository.findByUser(user)
                .orElseThrow(() -> new ScholarFundException("Institute profile not found. Please create one.", ErrorCode.NOT_FOUND, null));

        return mapToResponse(profile, user);
    }

    @Transactional
    public InstituteProfileResponse saveOrUpdateInstituteProfile(String email, InstituteProfileDto dto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ScholarFundException("User not found", ErrorCode.USER_NOT_FOUND, null));

        InstituteProfile profile = instituteProfileRepository.findByUser(user).orElse(new InstituteProfile());

        profile.setUser(user);
        profile.setInstituteName(dto.instituteName());
        profile.setUniversityAffiliation(dto.universityAffiliation());
        profile.setPrincipalName(dto.principalName());
        profile.setAddress(dto.address());

        // Validate College Code uniqueness
        if (dto.collegeCode() != null && !dto.collegeCode().equals(profile.getCollegeCode())) {
            if (instituteProfileRepository.existsByCollegeCode(dto.collegeCode())) {
                throw new ScholarFundException("College Code already registered to another institute", ErrorCode.ALREADY_EXIST, null);
            }
            profile.setCollegeCode(dto.collegeCode());
        }

        // Link Affiliation Certificate from DocumentFile table
        if (dto.affiliationCertificateFileId() != null) {
            DocumentFile certDoc = documentFileRepository.findById(dto.affiliationCertificateFileId())
                    .orElseThrow(() -> new ScholarFundException("Affiliation certificate reference not found.", ErrorCode.NOT_FOUND, null));
            profile.setAffiliationCertificateKey(certDoc.getS3Key());
        }

        InstituteProfile savedProfile = instituteProfileRepository.save(profile);
        log.info("Institute profile cleanly saved in database for user: {}", email);

        return mapToResponse(savedProfile, user);
    }

    private InstituteProfileResponse mapToResponse(InstituteProfile profile, User user) {

        String certUrl = null;
        if (profile.getAffiliationCertificateKey() != null) {
            certUrl = s3Service.generatePresignedUrl(profile.getAffiliationCertificateKey());
        }

        return new InstituteProfileResponse(
                profile.getId(),
                user.getEmail(),
                profile.getInstituteName(),
                profile.getCollegeCode(),
                profile.getUniversityAffiliation(),
                profile.getPrincipalName(),
                profile.getAddress(),
                certUrl
        );
    }
}
