package org.example.repository;

import org.example.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByJobIdAndIsHiddenFalse(Long jobId, Pageable pageable);
    
    Page<Review> findByRevieweeIdAndIsHiddenFalse(Long revieweeId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.job.id = :jobId AND r.reviewer.id = :reviewerId AND r.reviewee.id = :revieweeId")
    Optional<Review> findByJobAndReviewerAndReviewee(@Param("jobId") Long jobId, 
                                                      @Param("reviewerId") Long reviewerId,
                                                      @Param("revieweeId") Long revieweeId);
}
