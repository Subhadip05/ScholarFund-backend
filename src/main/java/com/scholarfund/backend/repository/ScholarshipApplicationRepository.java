package com.scholarfund.backend.repository;

import com.scholarfund.backend.entity.ScholarshipApplication;
import com.scholarfund.backend.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScholarshipApplicationRepository extends JpaRepository<ScholarshipApplication, Long> {

    // Spring translates this to "WHERE student_profile_id = ?"
    List<ScholarshipApplication> findByStudentProfileId(Long studentProfileId);

    // Spring translates this to "WHERE institute_profile_id = ?"
    List<ScholarshipApplication> findByInstituteProfileId(Long instituteProfileId);

    // Prevent students from applying multiple times in the same academic year
    boolean existsByStudentProfileAndAcademicYear(StudentProfile studentProfile, String academicYear);
}