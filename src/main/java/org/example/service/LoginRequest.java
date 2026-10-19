package org.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String identifier; // email or phone

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
