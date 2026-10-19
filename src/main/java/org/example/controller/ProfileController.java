package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.dto.StudentProfileDTO;
import org.example.dto.EmployerProfileDTO;
import org.example.dto.UpdateStudentProfileRequest;
import org.example.dto.UpdateEmployerProfileRequest;
import org.example.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/profiles")
@AllArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/students/{studentId}")
    public ResponseEntity<StudentProfileDTO> getStudentProfile(@PathVariable Long studentId) {
        StudentProfileDTO profile = profileService.getStudentProfile(studentId);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/students/{studentId}")
    public ResponseEntity<StudentProfileDTO> updateStudentProfile(
            @PathVariable Long studentId,
            @Valid @RequestBody UpdateStudentProfileRequest request) {
        StudentProfileDTO profile = profileService.updateStudentProfile(studentId, request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/employers/{employerId}")
    public ResponseEntity<EmployerProfileDTO> getEmployerProfile(@PathVariable Long employerId) {
        EmployerProfileDTO profile = profileService.getEmployerProfile(employerId);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/employers/{employerId}")
    public ResponseEntity<EmployerProfileDTO> updateEmployerProfile(
            @PathVariable Long employerId,
            @Valid @RequestBody UpdateEmployerProfileRequest request) {
        EmployerProfileDTO profile = profileService.updateEmployerProfile(employerId, request);
        return ResponseEntity.ok(profile);
    }
}
