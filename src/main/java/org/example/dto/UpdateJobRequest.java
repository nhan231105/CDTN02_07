package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateJobRequest {
    @Size(min=5,max=200) private String title;
    @Size(min=20,max=5000) private String description;
    @Size(max=2000) private String requirements;
    @Size(max=1000) private String benefits;
    @Size(min=1,max=100) private String category;
    @Size(min=1,max=100) private String location;
    @Size(min=1,max=200) private String workSchedule;
    @DecimalMin("0") @Digits(integer=12,fraction=0) private BigDecimal salary;
    @FutureOrPresent private LocalDate deadline;
}
