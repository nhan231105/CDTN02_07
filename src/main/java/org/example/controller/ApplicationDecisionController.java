package org.example.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.dto.ApplicationDTO;
import org.example.dto.ApplicationDecisionRequest;
import org.example.entity.Application;
import org.example.entity.ApplicationStatus;
import org.example.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
@AllArgsConstructor
public class ApplicationDecisionController {

    private final ApplicationService applicationService;

    @PatchMapping("/{applicationId}/decision")
    public ResponseEntity<ApplicationDTO> decide(
            @PathVariable Long applicationId,
            @RequestParam(required = false) Long employerId,
            @Valid @RequestBody ApplicationDecisionRequest request) {
        Application application;
        if (request.getStatus() == ApplicationStatus.APPROVED) {
            application = applicationService.approveApplication(applicationId, employerId);
        } else if (request.getStatus() == ApplicationStatus.REJECTED) {
            application = applicationService.rejectApplication(applicationId, employerId);
        } else {
            throw new IllegalArgumentException("Quyết định phải là APPROVED hoặc REJECTED");
        }
        return ResponseEntity.ok(applicationService.toDto(application));
    }
}
