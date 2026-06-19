package com.scholarfund.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documents_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentFile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName; // The original name (e.g., "my_aadhaar.pdf")

    @Column(nullable = false, unique = true)
    private String s3Key; // The exact path in AWS (e.g., "aadhaar/uuid.pdf")

    @Column(nullable = false)
    private String contentType; // e.g., "application/pdf" or "image/jpeg"

    private Long fileSize; // Size in bytes
}