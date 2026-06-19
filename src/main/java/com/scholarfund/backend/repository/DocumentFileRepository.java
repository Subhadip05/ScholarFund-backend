package com.scholarfund.backend.repository;

import com.scholarfund.backend.entity.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {
    Optional<DocumentFile> findByS3Key(String s3Key);
}