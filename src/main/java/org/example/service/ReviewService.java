package org.example.service;

import org.example.dto.CreateReviewRequest;
import org.example.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    
    Review createReview(Long jobId, Long reviewerId, Long revieweeId, CreateReviewRequest request);
    
    Review getReviewById(Long id);
    
    Page<Review> getJobReviews(Long jobId, Pageable pageable);
    
    Page<Review> getUserReviews(Long userId, Pageable pageable);
    
    boolean canReview(Long jobId, Long userId);
    
    Review getExistingReview(Long jobId, Long reviewerId, Long revieweeId);
}
