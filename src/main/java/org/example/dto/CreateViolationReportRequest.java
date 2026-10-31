package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateViolationReportRequest {
    @NotBlank
    private String targetType; // USER, JOB

    @NotNull
    private Long targetId;

    @NotBlank
    @Size(max = 500)
    private String reason;
}
