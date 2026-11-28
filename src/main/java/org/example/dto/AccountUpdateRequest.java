package org.example.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class AccountUpdateRequest {
    @NotBlank private String currentPassword;
    @Email @Size(max=150) private String email;
    @Pattern(regexp="^0\\d{9}$") private String phone;
    @Size(min=8,max=50) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).+$") private String password;
    /** Mã OTP 6 số gửi tới email hiện tại, bắt buộc khi đổi mật khẩu. */
    @Pattern(regexp="[0-9]{6}",message="Mã xác minh gồm 6 chữ số") private String code;
}
