package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.example.common.ApiResponse;
import org.example.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AccountEmailController {
    private final AccountEmailService accounts;

    @PostMapping("/register/verify")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> verify(@Valid @RequestBody RegistrationVerification request) {
        return ApiResponse.success(accounts.verifyRegistration(request.registration(), request.code()), "Email đã được xác minh.");
    }

    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> forgot(@Valid @RequestBody EmailRequest request) {
        accounts.requestReset(request.email());
        return ApiResponse.success("Nếu tài khoản có thể đặt lại mật khẩu, bạn sẽ nhận được email.");
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> reset(@Valid @RequestBody ResetRequest request) {
        accounts.reset(request.email(), request.code(), request.password());
        return ApiResponse.success("Đã đặt lại mật khẩu.");
    }

    public record RegistrationVerification(@NotNull @Valid RegisterRequest registration,
            @NotBlank @Pattern(regexp = "[0-9]{6}") String code) {}
    public record EmailRequest(@NotBlank @Email @Size(max = 150) String email) {}
    public record ResetRequest(@NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Pattern(regexp = "[0-9]{6}") String code,
            @NotBlank @Size(min = 8, max = 50)
            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).+$") String password) {}
}
