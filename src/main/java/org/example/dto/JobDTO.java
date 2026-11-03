package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Job;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private String category;
    private String location;
    private String workSchedule;
    private BigDecimal salary;
    private LocalDate deadline;
    private String status;
    private Long employerId;
    private String employerName;
    private Boolean removedByAdmin;
    private String removalReason;
    private String jobType;
    private String payType;
    private java.util.List<String> shifts;
    private Integer hoursPerWeek;
    private Boolean noExperienceRequired;
    private Boolean studentFriendly;
    private String address;
    private LocalDate startDate;
    private Integer durationMonths;
    private Integer headcount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double lat;
    private Double lng;
    private Double distanceKm;
    private String riskLevel;
    private java.util.List<String> riskFlags;

    public JobDTO(Job job) {
        this.jobType = job.getJobType() == null ? null : job.getJobType().name();
        this.payType = job.getPayType() == null ? null : job.getPayType().name();
        this.shifts = job.getShifts() == null || job.getShifts().isBlank() ? java.util.List.of() : java.util.Arrays.asList(job.getShifts().split(","));
        this.hoursPerWeek = job.getHoursPerWeek();
        this.noExperienceRequired = job.getNoExperienceRequired();
        this.studentFriendly = job.getStudentFriendly();
        this.address = job.getAddress();
        this.startDate = job.getStartDate();
        this.durationMonths = job.getDurationMonths();
        this.headcount = job.getHeadcount();
        this.lat = job.getLat();
        this.lng = job.getLng();
        this.id = job.getId();
        this.title = job.getTitle();
        this.description = job.getDescription();
        this.requirements = job.getRequirements();
        this.benefits = job.getBenefits();
        this.category = job.getCategory();
        this.location = job.getLocation();
        this.workSchedule = job.getWorkSchedule();
        this.salary = job.getSalary();
        this.deadline = job.getDeadline();
        this.status = job.getStatus().name();
        this.employerId = job.getEmployer().getId();
        this.employerName = job.getEmployer().getFullName();
        this.removedByAdmin = job.getRemovedByAdmin();
        this.removalReason = job.getRemovalReason();
        this.createdAt = job.getCreatedAt();
        this.updatedAt = job.getUpdatedAt();
        org.example.util.ScamSignals.Result risk = org.example.util.ScamSignals.analyze(job);
        this.riskLevel = risk.level().name();
        this.riskFlags = risk.flags();
    }
}
