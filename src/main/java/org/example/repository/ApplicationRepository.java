package org.example.repository;

import jakarta.persistence.LockModeType;
import org.example.entity.Application;
import org.example.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    long countByJobIdAndStudentId(Long jobId, Long studentId);
    long countByJobId(Long jobId);
    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);
    @Override @EntityGraph(attributePaths = {"job", "job.employer", "student"})
    Optional<Application> findById(Long id);
    @EntityGraph(attributePaths = {"job", "job.employer", "student"})
    Page<Application> findByJobIdOrderByCreatedAtDesc(Long jobId, Pageable pageable);
    @EntityGraph(attributePaths = {"job", "job.employer", "student"})
    Page<Application> findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable);
    @EntityGraph(attributePaths = {"job", "job.employer", "student"})
    Optional<Application> findByJobIdAndStudentId(Long jobId, Long studentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Application a join fetch a.job j join fetch j.employer join fetch a.student where a.id = :id")
    Optional<Application> findLockedById(@Param("id") Long id);
    @Query("select a.job.id from Application a where a.id = :id")
    Optional<Long> findJobId(@Param("id") Long id);
}
