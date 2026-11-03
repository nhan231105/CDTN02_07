package org.example.dto;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class MeProfileRequest {
    @Size(min=3,max=100) private String fullName;
    @Size(max=2000) private String bio;
    @Size(max=150) private String orgName;
    @Size(max=255) private String address;
    @Size(max=3000) private String description;
    @Size(max=150) private String school;
    @Size(max=150) private String major;
    @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(8) private Integer studyYear;
    @Size(max=500) private String skills;
    @Size(max=5) private java.util.List<@jakarta.validation.constraints.Pattern(regexp="MORNING|AFTERNOON|EVENING|WEEKEND|FLEXIBLE") String> availableShifts;
    @Size(max=100) private String preferredArea;
    private Boolean openToWork;
    @jakarta.validation.constraints.DecimalMin("-90") @jakarta.validation.constraints.DecimalMax("90") private Double lat;
    @jakarta.validation.constraints.DecimalMin("-180") @jakarta.validation.constraints.DecimalMax("180") private Double lng;
    private Boolean examMode;
    private java.time.LocalDate examStartDate;
    private java.time.LocalDate examEndDate;
    @Size(max=255) private String website;
    @Size(max=100) private String industry;
    @Size(max=50) private String companySize;
    @Size(max=150) private String tagline;
    @jakarta.validation.constraints.Min(1900) @jakarta.validation.constraints.Max(2100) private Integer foundedYear;
    @Size(max=600) private String benefits;
}
