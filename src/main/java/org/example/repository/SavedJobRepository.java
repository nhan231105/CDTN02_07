package org.example.repository;

import org.example.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);

    @EntityGraph(attributePaths = {"job", "job.employer"})
    Page<SavedJob> findByStudentIdAndJobRemovedByAdminFalse(Long studentId, Pageable pageable);

    @Query("select s.job.id from SavedJob s where s.student.id = :studentId and s.job.removedByAdmin = false")
    List<Long> findJobIdsByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("delete from SavedJob s where s.student.id = :studentId and s.job.id = :jobId")
    int deleteByStudentIdAndJobId(@Param("studentId") Long studentId, @Param("jobId") Long jobId);
}
