package org.example.repository;

import org.example.entity.Job;
import org.example.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findByStatusAndRemovedByAdminFalse(JobStatus status, Pageable pageable);
    
    Page<Job> findByEmployerIdAndRemovedByAdminFalse(Long employerId, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.removedByAdmin = false " +
           "AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.removedByAdmin = false " +
           "AND j.location = :location AND j.status = 'OPEN'")
    Page<Job> findByLocation(@Param("location") String location, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.removedByAdmin = false " +
           "AND j.category = :category AND j.status = 'OPEN'")
    Page<Job> findByCategory(@Param("category") String category, Pageable pageable);
    
    List<Job> findByEmployerIdAndRemovedByAdminFalse(Long employerId);
}
