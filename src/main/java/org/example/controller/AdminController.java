package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.dto.ViolationReportDTO;
import org.example.entity.ViolationReport;
import org.example.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> removeJob(
            @PathVariable Long jobId,
            @RequestParam String reason) {
        adminService.removeJob(jobId, reason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/lock")
    public ResponseEntity<Void> lockUser(
            @PathVariable Long userId,
            @RequestParam String reason) {
        adminService.lockUser(userId, reason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable Long userId) {
        adminService.unlockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<Page<ViolationReportDTO>> getViolationReports(Pageable pageable) {
        Page<ViolationReport> reports = adminService.getViolationReports(pageable);
        Page<ViolationReportDTO> dtos = reports.map(ViolationReportDTO::new);
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ViolationReportDTO> resolveViolation(
            @PathVariable Long reportId,
            @RequestParam String adminAction) {
        ViolationReport report = adminService.resolveViolation(reportId, adminAction);
        return ResponseEntity.ok(new ViolationReportDTO(report));
    }

    @PatchMapping("/reports/{reportId}/reject")
    public ResponseEntity<ViolationReportDTO> rejectViolation(@PathVariable Long reportId) {
        ViolationReport report = adminService.rejectViolation(reportId);
        return ResponseEntity.ok(new ViolationReportDTO(report));
    }
}
