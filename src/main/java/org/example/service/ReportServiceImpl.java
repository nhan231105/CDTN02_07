package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.DomainException;
import org.example.dto.CreateViolationReportRequest;
import org.example.entity.User;
import org.example.entity.UserRole;
import org.example.entity.ViolationReport;
import org.example.entity.ViolationStatus;
import org.example.repository.JobRepository;
import org.example.repository.UserRepository;
import org.example.repository.ViolationReportRepository;
import org.example.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private static final Set<String> TARGET_TYPES = Set.of("USER", "JOB");

    private final ViolationReportRepository violationReportRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional
    public ViolationReport submitReport(CreateViolationReportRequest request) {
        if (CurrentUser.role() != UserRole.STUDENT && CurrentUser.role() != UserRole.EMPLOYER) {
            throw new DomainException(403, "ROLE_FORBIDDEN", "Vai trò không có quyền gửi báo cáo vi phạm");
        }
        Long reporterId = CurrentUser.id();
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new DomainException(401, "UNAUTHENTICATED", "Vui lòng đăng nhập"));

        String targetType = request.getTargetType() == null
                ? ""
                : request.getTargetType().trim().toUpperCase(Locale.ROOT);
        if (!TARGET_TYPES.contains(targetType)) {
            throw new DomainException(400, "INVALID_TARGET_TYPE", "Loại đối tượng báo cáo không hợp lệ");
        }

        if (targetType.equals("USER")) {
            if (request.getTargetId().equals(reporterId)) {
                throw new DomainException(400, "CANNOT_REPORT_SELF", "Không thể tự báo cáo chính mình");
            }
            if (!userRepository.existsById(request.getTargetId())) {
                throw new DomainException(404, "TARGET_NOT_FOUND", "Không tìm thấy tài khoản cần báo cáo");
            }
        } else {
            if (!jobRepository.existsById(request.getTargetId())) {
                throw new DomainException(404, "TARGET_NOT_FOUND", "Không tìm thấy tin tuyển dụng cần báo cáo");
            }
        }

        ViolationReport report = new ViolationReport();
        report.setReporter(reporter);
        report.setTargetType(targetType);
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason().trim());
        report.setStatus(ViolationStatus.PENDING);

        ViolationReport saved = violationReportRepository.save(report);
        log.info("User {} submitted a {} report targeting {}", reporterId, targetType, request.getTargetId());
        return saved;
    }
}
