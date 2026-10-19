package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreateJobRequest;
import org.example.dto.UpdateJobRequest;
import org.example.entity.Job;
import org.example.entity.JobStatus;
import org.example.entity.User;
import org.example.repository.JobRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Job createJob(CreateJobRequest request, Long employerId) {
        User employer = userRepository.findById(employerId)
                .orElseThrow(() -> new UserNotFoundException("Nguoi tuyen dung khong ton tai"));

        Job job = new Job();
        job.setEmployer(employer);
        job.setTitle(request.getTitle());
        job.setCategory(request.getCategory());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setBenefits(request.getBenefits());
        job.setSalary(request.getSalary());
        job.setLocation(request.getLocation());
        job.setWorkSchedule(request.getWorkSchedule());
        job.setDeadline(request.getDeadline());
        job.setStatus(JobStatus.OPEN);

        Job savedJob = jobRepository.save(job);
        log.info("Job created with ID: {} by employer: {}", savedJob.getId(), employerId);
        return savedJob;
    }

    @Override
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .filter(job -> !job.getRemovedByAdmin())
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai hoac da bi xoa"));
    }

    @Override
    public Page<Job> searchJobs(String keyword, String category, String location, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return jobRepository.searchByKeyword(keyword, pageable);
        }
        if (category != null && !category.isEmpty()) {
            return jobRepository.findByCategory(category, pageable);
        }
        if (location != null && !location.isEmpty()) {
            return jobRepository.findByLocation(location, pageable);
        }
        return jobRepository.findByStatusAndRemovedByAdminFalse(JobStatus.OPEN, pageable);
    }

    @Override
    public Page<Job> getEmployerJobs(Long employerId, Pageable pageable) {
        return jobRepository.findByEmployerIdAndRemovedByAdminFalse(employerId, pageable);
    }

    @Override
    @Transactional
    public Job updateJob(Long jobId, Long employerId, UpdateJobRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai"));

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Chi chu cong viec moi co the sua");
        }

        if (!job.getStatus().equals(JobStatus.OPEN)) {
            throw new RuntimeException("Chi co the sua cong viec o trang thai OPEN");
        }

        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getCategory() != null) job.setCategory(request.getCategory());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getRequirements() != null) job.setRequirements(request.getRequirements());
        if (request.getBenefits() != null) job.setBenefits(request.getBenefits());
        if (request.getSalary() != null) job.setSalary(request.getSalary());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getWorkSchedule() != null) job.setWorkSchedule(request.getWorkSchedule());
        if (request.getDeadline() != null) job.setDeadline(request.getDeadline());

        Job updatedJob = jobRepository.save(job);
        log.info("Job {} updated by employer {}", jobId, employerId);
        return updatedJob;
    }

    @Override
    @Transactional
    public Job updateJobStatus(Long jobId, Long employerId, String newStatus) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai"));

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Chi chu cong viec moi co the doi trang thai");
        }

        JobStatus status = JobStatus.valueOf(newStatus.toUpperCase());
        job.setStatus(status);

        Job updatedJob = jobRepository.save(job);
        log.info("Job {} status updated to {} by employer {}", jobId, newStatus, employerId);
        return updatedJob;
    }

    @Override
    @Transactional
    public void removeJob(Long jobId, Long adminId, String reason) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai"));

        job.setRemovedByAdmin(true);
        job.setRemovalReason(reason);
        jobRepository.save(job);
        log.info("Job {} removed by admin {} for reason: {}", jobId, adminId, reason);
    }

    @Override
    public Page<Job> getAllJobs(Pageable pageable) {
        return jobRepository.findByStatusAndRemovedByAdminFalse(JobStatus.OPEN, pageable);
    }

    @Override
    public List<Job> getJobsNeedingReviews() {
        return jobRepository.findByStatusAndRemovedByAdminFalse(JobStatus.COMPLETED, Pageable.unpaged()).getContent();
    }
}
