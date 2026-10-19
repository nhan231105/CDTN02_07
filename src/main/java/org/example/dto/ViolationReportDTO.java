package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.ViolationReport;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationReportDTO {
    private Long id;
    private Long reporterId;
    private Long targetId;
    private String targetType;
    private String reason;
    private String status;
    private String adminAction;
    private LocalDateTime createdAt;

    public ViolationReportDTO(ViolationReport report) {
        this.id = report.getId();
        this.reporterId = report.getReporter().getId();
        this.targetId = report.getTargetId();
        this.targetType = report.getTargetType();
        this.reason = report.getReason();
        this.status = report.getStatus().name();
        this.adminAction = report.getAdminAction();
        this.createdAt = report.getCreatedAt();
    }
}
