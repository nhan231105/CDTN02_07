package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateApplicationRequest {
    @Size(max=100) private String fullName;
    @Email @Size(max=150) private String email;
    @Pattern(regexp="^0\\d{9}$", message="Số điện thoại không hợp lệ") private String phone;
    @Size(max=2000) private String coverLetter;
    @Size(max=255) private String cvFileRef;
    @NotNull @AssertTrue(message="Bạn cần đồng ý chia sẻ hồ sơ để ứng tuyển")
    private Boolean consentDataUsage;
    private Long studentId;
}
