package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Application;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    private Long id;
    private Long jobId;
    private Long studentId;
    private String jobTitle;
    private String jobStatus;
    private Long employerId;
    private String employerName;
    private boolean reviewSubmitted;
    private String fullName;
    private String email;
    private String phone;
    private String coverLetter;
    private String cvFileRef;
    private String status;
    private Boolean consentDataUsage;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;

    public ApplicationDTO(Application app) {
        this.id = app.getId();
        this.jobId = app.getJob().getId();
        this.studentId = app.getStudent().getId();
        this.jobTitle = app.getJob().getTitle();
        this.jobStatus = app.getJob().getStatus().name();
        this.employerId = app.getJob().getEmployer().getId();
        this.employerName = app.getJob().getEmployer().getFullName();
        this.fullName = app.getFullName();
        this.email = app.getEmail();
        this.phone = app.getPhone();
        this.coverLetter = app.getCoverLetter();
        this.cvFileRef = app.getCvFileRef();
        this.status = app.getStatus().name();
        this.consentDataUsage = app.getConsentDataUsage();
        this.createdAt = app.getCreatedAt();
        this.decidedAt = app.getDecidedAt();
    }
}
