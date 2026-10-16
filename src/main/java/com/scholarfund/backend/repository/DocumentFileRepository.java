package com.scholarfund.backend.repository;

import com.scholarfund.backend.entity.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {

}