package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.CreateReviewRequest;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.CurrentUser;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    @Override @Transactional
    public Review createApplicationReview(Long applicationId,CreateReviewRequest request) {
        CurrentUser.requireRole(UserRole.STUDENT);
        Long jobId=applicationRepository.findJobId(applicationId).orElseThrow(() -> new DomainException(404,"APPLICATION_NOT_FOUND","Không tìm thấy đơn ứng tuyển"));
        jobRepository.findLockedById(jobId).orElseThrow(() -> new DomainException(404,"JOB_NOT_FOUND","Không tìm thấy công việc"));
        Application application=applicationRepository.findLockedById(applicationId).orElseThrow(() -> new DomainException(404,"APPLICATION_NOT_FOUND","Không tìm thấy đơn ứng tuyển"));
        if (!application.getStudent().getId().equals(CurrentUser.id())) throw new DomainException(403,"NOT_APPLICATION_OWNER","Bạn không sở hữu đơn ứng tuyển này");
        return createReview(application.getJob().getId(),CurrentUser.id(),application.getJob().getEmployer().getId(),request);
    }
    @Override @Transactional
    public Review createReview(Long jobId,Long reviewerId,Long revieweeId,CreateReviewRequest request) {
        CurrentUser.requireId(reviewerId);
        if (CurrentUser.role()!=UserRole.STUDENT && CurrentUser.role()!=UserRole.EMPLOYER) throw new DomainException(403,"ROLE_FORBIDDEN","Vai trò không có quyền đánh giá");
        Job job=jobRepository.findLockedById(jobId).orElseThrow(() -> new DomainException(404,"JOB_NOT_FOUND","Không tìm thấy công việc"));
        if (job.getRemovedByAdmin() || job.getStatus()!=JobStatus.COMPLETED) throw new DomainException(409,"JOB_NOT_COMPLETED","Chỉ đánh giá khi công việc đã hoàn thành");
        Long current=CurrentUser.id();
        Long employer=job.getEmployer().getId();
        Long student;
        if (CurrentUser.role()==UserRole.STUDENT) {
            student=current;
            if (!employer.equals(revieweeId)) throw new DomainException(403,"REVIEW_FORBIDDEN","Bạn chỉ có thể đánh giá nhà tuyển dụng của công việc");
        } else {
            if (!current.equals(employer)) throw new DomainException(403,"NOT_JOB_OWNER","Bạn không sở hữu công việc này");
            student=revieweeId;
        }
        if (current.equals(revieweeId)) throw new DomainException(403,"SELF_REVIEW_FORBIDDEN","Không thể tự đánh giá");
        Application application=applicationRepository.findByJobIdAndStudentId(jobId,student).orElseThrow(() -> new DomainException(403,"REVIEW_FORBIDDEN","Người đánh giá phải tham gia công việc"));
        if (application.getStatus()!=ApplicationStatus.APPROVED) throw new DomainException(409,"APPLICATION_NOT_APPROVED","Đơn ứng tuyển chưa được duyệt");
        if (reviewRepository.existsByJobIdAndReviewerIdAndRevieweeId(jobId,current,revieweeId)) throw new DomainException(409,"ALREADY_REVIEWED","Bạn đã đánh giá công việc này");
        Review review=new Review(); review.setJob(job);
        review.setReviewer(userRepository.findById(current).orElseThrow(() -> new DomainException(401,"UNAUTHENTICATED","Vui lòng đăng nhập")));
        review.setReviewee(userRepository.findById(revieweeId).orElseThrow(() -> new DomainException(404,"USER_NOT_FOUND","Không tìm thấy người dùng")));
        review.setRating(request.getRating()); review.setContent(request.getContent().trim()); review.setIsHidden(false);
        return reviewRepository.saveAndFlush(review);
    }
    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id).filter(r -> !r.getIsHidden()).orElseThrow(() -> new DomainException(404,"REVIEW_NOT_FOUND","Không tìm thấy đánh giá"));
    }
    @Override public Page<Review> getJobReviews(Long jobId,Pageable pageable) { return reviewRepository.findByJobIdAndIsHiddenFalse(jobId,JobServiceImpl.page(pageable)); }
    @Override public Page<Review> getUserReviews(Long userId,Pageable pageable) { return reviewRepository.findByRevieweeIdAndIsHiddenFalse(userId,JobServiceImpl.page(pageable)); }
    @Override
    public boolean canReview(Long jobId,Long userId) {
        CurrentUser.requireId(userId);
        if (CurrentUser.role()!=UserRole.STUDENT) return false;
        Job job=jobRepository.findById(jobId).orElseThrow(() -> new DomainException(404,"JOB_NOT_FOUND","Không tìm thấy công việc"));
        if (job.getRemovedByAdmin() || job.getStatus()!=JobStatus.COMPLETED) return false;
        return applicationRepository.findByJobIdAndStudentId(jobId,CurrentUser.id()).filter(a -> a.getStatus()==ApplicationStatus.APPROVED).isPresent()
                && !reviewRepository.existsByJobIdAndReviewerIdAndRevieweeId(jobId,CurrentUser.id(),job.getEmployer().getId());
    }
    @Override public Review getExistingReview(Long jobId,Long reviewerId,Long revieweeId) { return reviewRepository.findByJobAndReviewerAndReviewee(jobId,reviewerId,revieweeId).orElse(null); }
}
