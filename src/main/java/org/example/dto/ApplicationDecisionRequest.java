package org.example.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import org.example.entity.ApplicationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDecisionRequest {
    @NotNull(message = "Quyết định không được để trống")
    @JsonAlias("decision")
    private ApplicationStatus status;
}
