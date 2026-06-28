package com.scholarfund.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scholarship_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScholarshipApplication extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links to the Student applying
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile;

    // Links to the Institute reviewing it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_profile_id", nullable = false)
    private InstituteProfile instituteProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false)
    private String courseName; // e.g., "B.Tech Computer Science"

    private String academicYear; // e.g., "2026-2027"

    @Column(columnDefinition = "TEXT")
    private String studentRemarks;

    @Column(columnDefinition = "TEXT")
    private String instituteRemarks;

    @Column(columnDefinition = "TEXT")
    private String govtRemarks;
}