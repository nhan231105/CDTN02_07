package org.example.service;

import org.example.dto.CreateApplicationRequest;
import org.example.entity.Application;
import org.example.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {
    
    Application createApplication(Long jobId, Long studentId, CreateApplicationRequest request);
    
    Application getApplicationById(Long id);
    
    Page<Application> getJobApplications(Long jobId, Long employerId, Pageable pageable);
    
    Page<Application> getStudentApplications(Long studentId, Pageable pageable);
    
    Application approveApplication(Long applicationId, Long employerId);
    
    Application rejectApplication(Long applicationId, Long employerId);
    
    boolean hasApplied(Long jobId, Long studentId);
    
    Application getByJobAndStudent(Long jobId, Long studentId);
}
