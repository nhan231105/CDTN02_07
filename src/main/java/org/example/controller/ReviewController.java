package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.dto.CreateReviewRequest;
import org.example.dto.ReviewDTO;
import org.example.entity.Review;
import org.example.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/jobs/{jobId}")
    public ResponseEntity<ReviewDTO> createReview(
            @PathVariable Long jobId,
            @RequestParam(required = false) Long reviewerId,
            @RequestParam Long revieweeId,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(jobId, reviewerId, revieweeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ReviewDTO(review));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Page<ReviewDTO>> getJobReviews(
            @PathVariable Long jobId,
            Pageable pageable) {
        Page<Review> reviews = reviewService.getJobReviews(jobId, pageable);
        Page<ReviewDTO> dtos = reviews.map(ReviewDTO::new);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<ReviewDTO>> getUserReviews(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<Review> reviews = reviewService.getUserReviews(userId, pageable);
        Page<ReviewDTO> dtos = reviews.map(ReviewDTO::new);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(new ReviewDTO(review));
    }
}
