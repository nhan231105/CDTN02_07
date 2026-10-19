package org.example.service;

import org.example.dto.CreateJobRequest;
import org.example.dto.UpdateJobRequest;
import org.example.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobService {
    
    Job createJob(CreateJobRequest request, Long employerId);
    
    Job getJobById(Long id);
    
    Page<Job> searchJobs(String keyword, String category, String location, Pageable pageable);
    
    Page<Job> getEmployerJobs(Long employerId, Pageable pageable);
    
    Job updateJob(Long jobId, Long employerId, UpdateJobRequest request);
    
    Job updateJobStatus(Long jobId, Long employerId, String newStatus);
    
    void removeJob(Long jobId, Long adminId, String reason);
    
    Page<Job> getAllJobs(Pageable pageable);
    
    List<Job> getJobsNeedingReviews();
}
