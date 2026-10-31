package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileDTO {
    private Long userId;
    private String avatarRef;
    private String bio;
    private String cvFileRef;
    private String cvOriginalName;
}
