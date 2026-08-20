package com.scholarfund.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_profile_id", nullable = false)
    private InstituteProfile instituteProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "application_number", unique = true, nullable = false, length = 30)
    private String applicationNumber;

    @Column(nullable = false)
    private String courseName;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "last_qualification_marks")
    private Double lastQualificationMarks;

    @Column(name = "last_qualification_course")
    private String lastQualificationCourse;

    @Column(name = "last_exam_rollno")
    private String lastQualificationExamRollNo;

    @Column(name = "pass_out_board_name")
    private String passOutBoardName;

    @Column(name = "annual_income")
    private Double annualIncome;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    // AWS S3 FILE KEYS : incomeCertificateKey,hsMarksheetKey, bankPassbookKey
    // Store the path of the file into DocumentFile table, not the file itself

    @Column(name = "income_certificate_key")
    private String incomeCertificateKey;

    @Column(name = "hs_marksheet_key")
    private String hsMarksheetKey;

    @Column(name = "bank_passbook_key")
    private String bankPassbookKey;

    @Column(name = "admission_receipt_key")
    private String admissionReceiptKey;

    // CascadeType.ALL means when save the Application, it automatically saves the History records inside it.
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApplicationHistory> trackRecords = new ArrayList<>();
}