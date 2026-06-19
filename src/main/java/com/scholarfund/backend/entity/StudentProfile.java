package com.scholarfund.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "aadhaar_number", unique = true, length = 12)
    private String aadhaarNumber;

    // AWS S3 FILE KEYS : aadhaarDocumentKey, incomeCertificateKey
    // Store the path to the file, not the file itself

    @Column(name = "aadhaar_document_key")
    private String aadhaarDocumentKey;

    @Column(name = "income_certificate_key")
    private String incomeCertificateKey;

}