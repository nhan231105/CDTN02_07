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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobDTO(Job job) {
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
        this.createdAt = job.getCreatedAt();
        this.updatedAt = job.getUpdatedAt();
    }
}
