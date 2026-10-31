package org.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String accessToken;
    private String tokenType;
    private java.time.Instant expiresAt;
}
