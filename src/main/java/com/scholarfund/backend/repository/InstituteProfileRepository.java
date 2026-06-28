package com.scholarfund.backend.repository;

import com.scholarfund.backend.entity.InstituteProfile;
import com.scholarfund.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstituteProfileRepository extends JpaRepository<InstituteProfile, Long> {
    Optional<InstituteProfile> findByUser(User user);
    boolean existsByCollegeCode(String collegeCode);
}
