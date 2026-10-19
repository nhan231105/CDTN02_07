package org.example.repository;

import org.example.entity.ViolationReport;
import org.example.entity.ViolationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViolationReportRepository extends JpaRepository<ViolationReport, Long> {
    
    Page<ViolationReport> findByStatusOrderByCreatedAtDesc(ViolationStatus status, Pageable pageable);
}
