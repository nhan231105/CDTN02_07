package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.dto.ApplicationDTO;
import org.example.dto.ApplicationDecisionRequest;
import org.example.dto.CreateApplicationRequest;
import org.example.entity.Application;
import org.example.service.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/jobs/{jobId}/applications")
@AllArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationDTO> applyForJob(
            @PathVariable Long jobId,
            @Valid @RequestBody CreateApplicationRequest request) {
        Application application = applicationService.createApplication(jobId, request.getStudentId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApplicationDTO(application));
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationDTO>> getJobApplications(
            @PathVariable Long jobId,
            @RequestParam Long employerId,
            Pageable pageable) {
        Page<Application> applications = applicationService.getJobApplications(jobId, employerId, pageable);
        Page<ApplicationDTO> dtos = applications.map(ApplicationDTO::new);
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<ApplicationDTO> approveApplication(
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            @RequestParam Long employerId) {
        Application application = applicationService.approveApplication(applicationId, employerId);
        return ResponseEntity.ok(new ApplicationDTO(application));
    }

    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<ApplicationDTO> rejectApplication(
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            @RequestParam Long employerId) {
        Application application = applicationService.rejectApplication(applicationId, employerId);
        return ResponseEntity.ok(new ApplicationDTO(application));
    }
}
