package org.example.repository;

import org.example.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Override @EntityGraph(attributePaths = {"job", "reviewer", "reviewee"})
    Optional<Review> findById(Long id);
    @EntityGraph(attributePaths = {"job", "reviewer", "reviewee"})
    Page<Review> findByJobIdAndIsHiddenFalse(Long jobId, Pageable pageable);
    @EntityGraph(attributePaths = {"job", "reviewer", "reviewee"})
    Page<Review> findByRevieweeIdAndIsHiddenFalse(Long revieweeId, Pageable pageable);
    @EntityGraph(attributePaths = {"job", "reviewer", "reviewee"})
    @Query("select r from Review r where r.job.id = :jobId and r.reviewer.id = :reviewerId and r.reviewee.id = :revieweeId")
    Optional<Review> findByJobAndReviewerAndReviewee(@Param("jobId") Long jobId, @Param("reviewerId") Long reviewerId, @Param("revieweeId") Long revieweeId);
    boolean existsByJobIdAndReviewerIdAndRevieweeId(Long jobId, Long reviewerId, Long revieweeId);
}
