package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Review;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long jobId;
    private Long reviewerId;
    private Long revieweeId;
    private Integer rating;
    private String content;
    private Boolean isHidden;
    private LocalDateTime createdAt;

    public ReviewDTO(Review review) {
        this.id = review.getId();
        this.jobId = review.getJob().getId();
        this.reviewerId = review.getReviewer().getId();
        this.revieweeId = review.getReviewee().getId();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.isHidden = review.getIsHidden();
        this.createdAt = review.getCreatedAt();
    }
}
