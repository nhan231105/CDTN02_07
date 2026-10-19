package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Job;
import org.example.entity.JobStatus;
import org.example.entity.User;
import org.example.entity.UserStatus;
import org.example.entity.ViolationReport;
import org.example.entity.ViolationStatus;
import org.example.repository.JobRepository;
import org.example.repository.UserRepository;
import org.example.repository.ViolationReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ViolationReportRepository violationReportRepository;

    @Override
    @Transactional
    public void removeJob(Long jobId, String reason) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai"));

        job.setRemovedByAdmin(true);
        job.setRemovalReason(reason);
        job.setStatus(JobStatus.COMPLETED);

        jobRepository.save(job);
        log.info("Job {} removed by admin. Reason: {}", jobId, reason);
    }

    @Override
    @Transactional
    public void lockUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nguoi dung khong ton tai"));

        user.setStatus(UserStatus.LOCKED);
        user.setLockReason(reason);

        userRepository.save(user);
        log.info("User {} locked by admin. Reason: {}", userId, reason);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nguoi dung khong ton tai"));

        user.setStatus(UserStatus.ACTIVE);
        user.setLockReason(null);

        userRepository.save(user);
        log.info("User {} unlocked by admin", userId);
    }

    @Override
    public Page<ViolationReport> getViolationReports(Pageable pageable) {
        return violationReportRepository.findByStatusOrderByCreatedAtDesc(ViolationStatus.PENDING, pageable);
    }

    @Override
    @Transactional
    public ViolationReport resolveViolation(Long violationId, String adminAction) {
        ViolationReport report = violationReportRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Bao cao khong ton tai"));

        report.setStatus(ViolationStatus.RESOLVED);
        report.setAdminAction(adminAction);

        ViolationReport saved = violationReportRepository.save(report);
        log.info("Violation {} resolved with action: {}", violationId, adminAction);
        return saved;
    }

    @Override
    @Transactional
    public ViolationReport rejectViolation(Long violationId) {
        ViolationReport report = violationReportRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Bao cao khong ton tai"));

        report.setStatus(ViolationStatus.REJECTED);

        ViolationReport saved = violationReportRepository.save(report);
        log.info("Violation {} rejected", violationId);
        return saved;
    }
}
