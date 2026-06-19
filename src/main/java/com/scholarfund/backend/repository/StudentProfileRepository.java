package com.scholarfund.backend.repository;

import com.scholarfund.backend.entity.StudentProfile;
import com.scholarfund.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser(User user);
    boolean existsByAadhaarNumber(String aadhaarNumber);

}