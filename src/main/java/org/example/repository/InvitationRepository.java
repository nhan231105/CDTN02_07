package org.example.repository;

import org.example.entity.Invitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    boolean existsByJobIdAndStudentId(Long jobId, Long studentId);
    long countByEmployerIdAndCreatedAtAfter(Long employerId, LocalDateTime since);
    @EntityGraph(attributePaths = {"employer", "job"})
    Page<Invitation> findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable);
    @EntityGraph(attributePaths = {"student", "job"})
    Page<Invitation> findByEmployerIdOrderByCreatedAtDesc(Long employerId, Pageable pageable);
    @EntityGraph(attributePaths = {"employer", "job"})
    List<Invitation> findTop100ByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Invitation> findByEmployerIdAndStudentId(Long employerId, Long studentId);
    long countByStudentIdAndStatus(Long studentId, org.example.entity.InvitationStatus status);
}
