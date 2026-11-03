package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateJobRequest {
    @NotBlank @Size(min=5,max=200) private String title;
    @NotBlank @Size(min=20,max=5000) private String description;
    @Size(max=2000) private String requirements;
    @Size(max=1000) private String benefits;
    @NotBlank @Size(max=100) private String category;
    @NotBlank @Size(max=100) private String location;
    @NotBlank @Size(max=200) private String workSchedule;
    @NotNull @DecimalMin("0") @Digits(integer=12,fraction=0) private BigDecimal salary;
    @NotNull @FutureOrPresent private LocalDate deadline;
    @Pattern(regexp="PART_TIME|SEASONAL|INTERNSHIP|FREELANCE") private String jobType;
    @Pattern(regexp="HOURLY|SHIFT|DAILY|MONTHLY|PROJECT") private String payType;
    @Size(max=5) private java.util.List<@Pattern(regexp="MORNING|AFTERNOON|EVENING|WEEKEND|FLEXIBLE") String> shifts;
    @Min(1) @Max(80) private Integer hoursPerWeek;
    private Boolean noExperienceRequired;
    private Boolean studentFriendly;
    @Size(max=255) private String address;
    private LocalDate startDate;
    @Min(1) @Max(60) private Integer durationMonths;
    @Min(1) @Max(1000) private Integer headcount;
    private Long employerId;
    @DecimalMin("-90") @DecimalMax("90") private Double lat;
    @DecimalMin("-180") @DecimalMax("180") private Double lng;
}
