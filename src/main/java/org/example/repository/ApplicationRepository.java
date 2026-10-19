package org.example.repository;

import org.example.entity.Application;
import org.example.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId AND a.student.id = :studentId")
    long countByJobIdAndStudentId(@Param("jobId") Long jobId, @Param("studentId") Long studentId);
    
    Page<Application> findByJobIdOrderByCreatedAtDesc(Long jobId, Pageable pageable);
    
    Page<Application> findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable);
    
    Optional<Application> findByJobIdAndStudentId(Long jobId, Long studentId);
}
