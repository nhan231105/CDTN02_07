package org.example.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.dto.CreateViolationReportRequest;
import org.example.dto.ViolationReportDTO;
import org.example.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@AllArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ViolationReportDTO> submit(@Valid @RequestBody CreateViolationReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ViolationReportDTO(reportService.submitReport(request)));
    }
}
