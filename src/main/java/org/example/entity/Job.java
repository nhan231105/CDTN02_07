package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_location", columnList = "location"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_employer", columnList = "employer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(precision = 12, scale = 0)
    private BigDecimal salary;

    @Column(length = 100)
    private String location;

    @Column(length = 200)
    private String workSchedule;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobType jobType = JobType.PART_TIME;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayType payType = PayType.HOURLY;

    // Comma separated JobShift names, e.g. MORNING,EVENING
    @Column(length = 100)
    private String shifts;

    private Integer hoursPerWeek;

    @Column(nullable = false)
    private Boolean noExperienceRequired = false;

    @Column(nullable = false)
    private Boolean studentFriendly = true;

    @Column(length = 255)
    private String address;

    private LocalDate startDate;

    private Integer durationMonths;

    @Column(nullable = false)
    private Integer headcount = 1;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.OPEN;

    @Column(nullable = false)
    private Boolean removedByAdmin = false;

    @Column(length = 500)
    private String removalReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
