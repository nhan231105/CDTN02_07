package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.*;
import org.example.entity.Application;
import org.example.service.ApplicationService;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs/{jobId}/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    @PostMapping
    public ResponseEntity<ApplicationDTO> apply(@PathVariable Long jobId,@Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.status(201).body(applicationService.toDto(applicationService.createApplication(jobId,request.getStudentId(),request)));
    }
    @GetMapping
    public Page<ApplicationDTO> list(@PathVariable Long jobId,@RequestParam(required=false) Long employerId,@PageableDefault(size=12) Pageable pageable) {
        return applicationService.getJobApplications(jobId,employerId,pageable).map(applicationService::toDto);
    }
    @PatchMapping("/{applicationId}/approve")
    public ApplicationDTO approve(@PathVariable Long jobId,@PathVariable Long applicationId,@RequestParam(required=false) Long employerId) {
        checkJob(jobId,applicationId);
        return applicationService.toDto(applicationService.approveApplication(applicationId,employerId));
    }
    @PatchMapping("/{applicationId}/reject")
    public ApplicationDTO reject(@PathVariable Long jobId,@PathVariable Long applicationId,@RequestParam(required=false) Long employerId) {
        checkJob(jobId,applicationId);
        return applicationService.toDto(applicationService.rejectApplication(applicationId,employerId));
    }
    private void checkJob(Long jobId,Long applicationId) {
        Application application=applicationService.getApplicationById(applicationId);
        if (!application.getJob().getId().equals(jobId)) throw new DomainException(404,"APPLICATION_NOT_FOUND","Đơn không thuộc tin tuyển dụng này");
    }
}
