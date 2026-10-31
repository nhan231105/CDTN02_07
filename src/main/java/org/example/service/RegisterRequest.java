package org.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(min = 3, max = 100, message = "Tên phải từ 3 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 50, message = "Mật khẩu phải từ 8 đến 50 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).+$", message = "Mật khẩu cần chữ hoa, chữ thường và ký tự đặc biệt")
    private String password;

    @NotBlank(message = "Vai trò không được để trống")
    private String role; // STUDENT or EMPLOYER
}
