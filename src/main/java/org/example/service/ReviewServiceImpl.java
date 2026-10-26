package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreateReviewRequest;
import org.example.entity.Application;
import org.example.entity.ApplicationStatus;
import org.example.entity.Job;
import org.example.entity.JobStatus;
import org.example.entity.Review;
import org.example.entity.User;
import org.example.repository.JobRepository;
import org.example.repository.ReviewRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationService applicationService;

    @Override
    @Transactional
    public Review createReview(Long jobId, Long reviewerId, Long revieweeId, CreateReviewRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai"));

        if (!job.getStatus().equals(JobStatus.COMPLETED)) {
            throw new RuntimeException("Chi co the danh gia khi cong viec hoan thanh");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new UserNotFoundException("Nguoi danh gia khong ton tai"));

        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new UserNotFoundException("Nguoi duoc danh gia khong ton tai"));

        if (reviewerId.equals(revieweeId)) {
            throw new RuntimeException("Khong the tu danh gia chinh minh");
        }

        Long employerId = job.getEmployer().getId();
        Long studentId;
        if (reviewerId.equals(employerId)) {
            studentId = revieweeId;
        } else if (revieweeId.equals(employerId)) {
            studentId = reviewerId;
        } else {
            throw new RuntimeException("Danh gia phai giua nha tuyen dung cua cong viec va sinh vien duoc duyet");
        }

        Application app = applicationService.getByJobAndStudent(jobId, studentId);
        if (app == null || app.getStatus() != ApplicationStatus.APPROVED) {
            throw new RuntimeException("Chi sinh vien da duoc duyet cho cong viec nay moi duoc danh gia");
        }

        if (reviewRepository.findByJobAndReviewerAndReviewee(jobId, reviewerId, revieweeId).isPresent()) {
            throw new RuntimeException("Ban da danh gia nguoi nay cho cong viec nay roi");
        }

        Review review = new Review();
        review.setJob(job);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setIsHidden(false);

        Review savedReview = reviewRepository.save(review);
        log.info("Review created - ID: {}, Job: {}, Reviewer: {}, Reviewee: {}", 
                 savedReview.getId(), jobId, reviewerId, revieweeId);

        return savedReview;
    }

    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .filter(r -> !r.getIsHidden())
                .orElseThrow(() -> new RuntimeException("Danh gia khong ton tai"));
    }

    @Override
    public Page<Review> getJobReviews(Long jobId, Pageable pageable) {
        return reviewRepository.findByJobIdAndIsHiddenFalse(jobId, pageable);
    }

    @Override
    public Page<Review> getUserReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByRevieweeIdAndIsHiddenFalse(userId, pageable);
    }

    @Override
    public boolean canReview(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai"));

        if (!job.getStatus().equals(JobStatus.COMPLETED)) {
            return false;
        }

        return true;
    }

    @Override
    public Review getExistingReview(Long jobId, Long reviewerId, Long revieweeId) {
        return reviewRepository.findByJobAndReviewerAndReviewee(jobId, reviewerId, revieweeId)
                .orElse(null);
    }
}
