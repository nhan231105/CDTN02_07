package org.example.service;

import org.example.dto.CreateViolationReportRequest;
import org.example.entity.ViolationReport;

public interface ReportService {

    ViolationReport submitReport(CreateViolationReportRequest request);
}
