package org.example.repository;

import jakarta.persistence.LockModeType;
import org.example.entity.Job;
import org.example.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    @Override @EntityGraph(attributePaths = {"employer"})
    Optional<Job> findById(Long id);
    @Override @EntityGraph(attributePaths = {"employer"})
    Page<Job> findAll(Specification<Job> specification, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from Job j join fetch j.employer where j.id = :id")
    Optional<Job> findLockedById(@Param("id") Long id);
    @EntityGraph(attributePaths = {"employer"})
    Page<Job> findByStatusAndRemovedByAdminFalse(JobStatus status, Pageable pageable);
    @EntityGraph(attributePaths = {"employer"})
    Page<Job> findByEmployerIdAndRemovedByAdminFalse(Long employerId, Pageable pageable);
    List<Job> findByEmployerIdAndRemovedByAdminFalse(Long employerId);
}
