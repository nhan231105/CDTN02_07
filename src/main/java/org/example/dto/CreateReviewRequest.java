package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotNull @Min(1) @Max(5) private Integer rating;
    @NotBlank @Size(max=2000) private String content;
}
