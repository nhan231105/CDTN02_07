package org.example.service;

import org.example.entity.ViolationReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    
    void removeJob(Long jobId, String reason);
    
    void lockUser(Long userId, String reason);
    
    void unlockUser(Long userId);
    
    Page<ViolationReport> getViolationReports(Pageable pageable);
    
    ViolationReport resolveViolation(Long violationId, String adminAction);
    
    ViolationReport rejectViolation(Long violationId);
}
