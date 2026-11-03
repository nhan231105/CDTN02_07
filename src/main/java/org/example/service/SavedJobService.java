package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.SavedJobDTO;
import org.example.entity.*;
import org.example.repository.JobRepository;
import org.example.repository.SavedJobRepository;
import org.example.repository.UserRepository;
import org.example.security.CurrentUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedJobService {
    private final SavedJobRepository savedJobs;
    private final JobRepository jobs;
    private final UserRepository users;

    public Page<SavedJobDTO> list(Pageable pageable) {
        CurrentUser.requireRole(UserRole.STUDENT);
        Pageable page = PageRequest.of(Math.max(0, pageable.getPageNumber()), Math.max(1, Math.min(12, pageable.getPageSize())),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return savedJobs.findByStudentIdAndJobRemovedByAdminFalse(CurrentUser.id(), page).map(SavedJobDTO::of);
    }

    public List<Long> ids() {
        CurrentUser.requireRole(UserRole.STUDENT);
        return savedJobs.findJobIdsByStudentId(CurrentUser.id());
    }

    @Transactional
    public void save(Long jobId) {
        CurrentUser.requireRole(UserRole.STUDENT);
        Long studentId = CurrentUser.id();
        Job job = jobs.findById(jobId).orElseThrow(() -> new DomainException(404, "JOB_NOT_FOUND", "Không tìm thấy công việc"));
        if (Boolean.TRUE.equals(job.getRemovedByAdmin()) || job.getStatus() != JobStatus.OPEN
                || job.getDeadline().isBefore(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))))
            throw new DomainException(409, "JOB_NOT_SAVABLE", "Tin tuyển dụng đã đóng hoặc hết hạn, không thể lưu");
        if (savedJobs.existsByStudentIdAndJobId(studentId, jobId)) return;
        User student = users.findById(studentId).orElseThrow(() -> new DomainException(401, "UNAUTHENTICATED", "Vui lòng đăng nhập"));
        SavedJob saved = new SavedJob();
        saved.setStudent(student);
        saved.setJob(job);
        try {
            savedJobs.saveAndFlush(saved);
        } catch (DataIntegrityViolationException duplicate) {
            // A concurrent request already saved it; the operation is idempotent.
        }
    }

    @Transactional
    public void remove(Long jobId) {
        CurrentUser.requireRole(UserRole.STUDENT);
        savedJobs.deleteByStudentIdAndJobId(CurrentUser.id(), jobId);
    }
}
