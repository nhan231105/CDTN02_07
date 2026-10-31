package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import org.example.entity.JobStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusUpdateRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private JobStatus status;
}
