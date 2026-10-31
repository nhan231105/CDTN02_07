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
}
