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

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone_number", unique = true, length = 10)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "aadhaar_number", unique = true, length = 12)
    private String aadhaarNumber;

    @Column(name = "is_west_bengal_resident")
    private Boolean isWestBengalResident;

    // AWS S3 FILE KEYS : aadhaarDocumentKey,selfImageKey, selfSignatureKey
    // Store the path of the file into DocumentFile table, not the file itself

    @Column(name = "aadhaar_document_key")
    private String aadhaarDocumentKey;

    @Column(name = "self_image_key")
    private String selfImageKey;

    @Column(name = "self_signature_key")
    private String selfSignatureKey;
}