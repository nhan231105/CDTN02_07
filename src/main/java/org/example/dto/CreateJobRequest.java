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
    private Long employerId;
}
