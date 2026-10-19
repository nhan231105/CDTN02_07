package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileDTO {
    private Long userId;
    private String orgName;
    private String address;
    private String description;
    private String logoRef;
}
