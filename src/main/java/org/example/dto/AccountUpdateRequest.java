package org.example.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class AccountUpdateRequest {
    @NotBlank private String currentPassword;
    @Email @Size(max=150) private String email;
    @Pattern(regexp="^0\\d{9}$") private String phone;
    @Size(min=8,max=50) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).+$") private String password;
}
