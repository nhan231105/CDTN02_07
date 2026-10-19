package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentProfileRequest {
    @Size(max = 255, message = "Avatar không được vượt quá 255 ký tự")
    private String avatarRef;

    @Size(max = 2000, message = "Tiểu sử không được vượt quá 2000 ký tự")
    private String bio;

    @Size(max = 255, message = "CV không được vượt quá 255 ký tự")
    private String cvFileRef;
}
