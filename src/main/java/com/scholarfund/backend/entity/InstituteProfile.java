package com.scholarfund.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "institute_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstituteProfile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String instituteName;

    @Column(nullable = false, unique = true)
    private String collegeCode; // e.g., "WB-TECH-001"

    private String universityAffiliation; // e.g., "Calcutta University"

    private String principalName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "verified_by_govt", nullable = false)
    @Builder.Default
    private Boolean verifiedByGovt = false;

    private String affiliationCertificateKey;
}