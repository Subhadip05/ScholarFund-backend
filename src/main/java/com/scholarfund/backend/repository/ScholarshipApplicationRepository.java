package com.scholarfund.backend.repository;

import com.scholarfund.backend.entity.InstituteProfile;
import com.scholarfund.backend.entity.ScholarshipApplication;
import com.scholarfund.backend.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScholarshipApplicationRepository extends JpaRepository<ScholarshipApplication, Long> {

    // Fetch all applications for a specific student
    List<ScholarshipApplication> findByStudentProfile(StudentProfile studentProfile);

    // Fetch all applications for a specific institute
    List<ScholarshipApplication> findByInstituteProfile(InstituteProfile instituteProfile);

    // Spring translates this to "WHERE student_profile_id = ?"
    List<ScholarshipApplication> findByStudentProfileId(Long studentProfileId);

    // Spring translates this to "WHERE institute_profile_id = ?"
    List<ScholarshipApplication> findByInstituteProfileId(Long instituteProfileId);

    // Prevent students from applying multiple times in the same academic year
    boolean existsByStudentProfileAndAcademicYear(StudentProfile studentProfile, String academicYear);
}