package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.DomainException;
import org.example.entity.Job;
import org.example.entity.User;
import org.example.entity.UserRole;
import org.example.entity.UserStatus;
import org.example.entity.ViolationReport;
import org.example.entity.ViolationStatus;
import org.example.repository.ApplicationRepository;
import org.example.repository.EmployerProfileRepository;
import org.example.repository.JobRepository;
import org.example.repository.NotificationRepository;
import org.example.repository.ReviewRepository;
import org.example.repository.StudentProfileRepository;
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
    private final ApplicationRepository applicationRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    @Override
    @Transactional
    public void removeJob(Long jobId, String reason) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new DomainException(404, "JOB_NOT_FOUND", "Công việc không tồn tại"));

        job.setRemovedByAdmin(true);
        job.setRemovalReason(reason);

        jobRepository.save(job);
        log.info("Job {} removed by admin. Reason: {}", jobId, reason);
    }

    @Override
    @Transactional
    public void restoreJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new DomainException(404, "JOB_NOT_FOUND", "Công việc không tồn tại"));

        job.setRemovedByAdmin(false);
        job.setRemovalReason(null);

        jobRepository.save(job);
        log.info("Job {} restored by admin", jobId);
    }

    @Override
    public Page<Job> getAllJobsForModeration(Pageable pageable) {
        return jobRepository.findAllForModeration(pageable);
    }

    @Override
    @Transactional
    public void lockUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        user.setStatus(UserStatus.LOCKED);
        user.setLockReason(reason);

        userRepository.save(user);
        log.info("User {} locked by admin. Reason: {}", userId, reason);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        user.setStatus(UserStatus.ACTIVE);
        user.setLockReason(null);

        userRepository.save(user);
        log.info("User {} unlocked by admin", userId);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new DomainException(403, "CANNOT_DELETE_ADMIN", "Không thể xóa tài khoản quản trị viên");
        }

        boolean hasActivity = jobRepository.countByEmployerId(userId) > 0
                || applicationRepository.countByStudentId(userId) > 0
                || reviewRepository.countByReviewerIdOrRevieweeId(userId, userId) > 0
                || violationReportRepository.countByReporterId(userId) > 0;
        if (hasActivity) {
            throw new DomainException(409, "USER_HAS_DATA",
                    "Tài khoản đã có hoạt động trên hệ thống (tin tuyển dụng, đơn ứng tuyển, đánh giá hoặc báo cáo). Vui lòng khóa tài khoản thay vì xóa.");
        }

        notificationRepository.deleteByUserId(userId);
        if (user.getRole() == UserRole.STUDENT) {
            studentProfileRepository.deleteById(userId);
        } else if (user.getRole() == UserRole.EMPLOYER) {
            employerProfileRepository.deleteById(userId);
        }
        userRepository.delete(user);
        log.info("User {} deleted by admin", userId);
    }

    @Override
    public Page<ViolationReport> getViolationReports(Pageable pageable) {
        return violationReportRepository.findByStatusOrderByCreatedAtDesc(ViolationStatus.PENDING, pageable);
    }

    @Override
    @Transactional
    public ViolationReport resolveViolation(Long violationId, String adminAction) {
        ViolationReport report = violationReportRepository.findById(violationId)
                .orElseThrow(() -> new DomainException(404, "REPORT_NOT_FOUND", "Báo cáo không tồn tại"));

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
                .orElseThrow(() -> new DomainException(404, "REPORT_NOT_FOUND", "Báo cáo không tồn tại"));

        report.setStatus(ViolationStatus.REJECTED);

        ViolationReport saved = violationReportRepository.save(report);
        log.info("Violation {} rejected", violationId);
        return saved;
    }
}

