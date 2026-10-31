package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.*;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    @Override @Transactional
    public Application createApplication(Long jobId,Long studentId,CreateApplicationRequest request) {
        CurrentUser.requireRole(UserRole.STUDENT); CurrentUser.requireId(studentId);
        Job job=jobRepository.findLockedById(jobId).orElseThrow(() -> new DomainException(404,"JOB_NOT_FOUND","Không tìm thấy công việc"));
        if (job.getRemovedByAdmin() || job.getStatus()!=JobStatus.OPEN || job.getDeadline().isBefore(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))))
            throw new DomainException(409,"JOB_NOT_OPEN","Tin không còn nhận hồ sơ");
        if (hasApplied(jobId,CurrentUser.id())) throw new DomainException(409,"ALREADY_APPLIED","Bạn đã ứng tuyển công việc này");
        if (!Boolean.TRUE.equals(request.getConsentDataUsage())) throw new DomainException(400,"CONSENT_REQUIRED","Bạn cần đồng ý chia sẻ hồ sơ");
        User student=userRepository.findById(CurrentUser.id()).orElseThrow(() -> new DomainException(401,"UNAUTHENTICATED","Vui lòng đăng nhập"));
        String cv=request.getCvFileRef();
        if (cv==null || cv.isBlank()) cv=fileStorageService.getCurrentCv(student.getId());
        if (cv==null || cv.isBlank()) throw new DomainException(400,"CV_REQUIRED","Vui lòng tải CV trước khi ứng tuyển");
        fileStorageService.requireOwnedCv(cv,student.getId());
        Application application=new Application();
        application.setJob(job); application.setStudent(student); application.setCvFileRef(cv);
        application.setFullName(value(request.getFullName(),student.getFullName()));
        application.setEmail(value(request.getEmail(),student.getEmail()));
        application.setPhone(value(request.getPhone(),student.getPhone()));
        application.setCoverLetter(request.getCoverLetter()); application.setConsentDataUsage(true); application.setStatus(ApplicationStatus.PENDING);
        Application saved=applicationRepository.saveAndFlush(application);
        notificationService.createNotification(job.getEmployer().getId(),"Có ứng tuyển mới cho công việc: "+job.getTitle(),"APPLICATION",saved.getId());
        return saved;
    }
    private static String value(String candidate,String fallback) { return candidate==null || candidate.isBlank()? fallback:candidate.trim(); }
    @Override
    public Application getApplicationById(Long id) {
        Application application=applicationRepository.findById(id).orElseThrow(() -> new DomainException(404,"APPLICATION_NOT_FOUND","Không tìm thấy đơn ứng tuyển"));
        Long current=CurrentUser.id();
        if (!application.getStudent().getId().equals(current) && !application.getJob().getEmployer().getId().equals(current))
            throw new DomainException(403,"APPLICATION_FORBIDDEN","Bạn không có quyền xem đơn này");
        return application;
    }
    @Override
    public Page<Application> getJobApplications(Long jobId,Long employerId,Pageable pageable) {
        CurrentUser.requireRole(UserRole.EMPLOYER); CurrentUser.requireId(employerId);
        Job job=jobRepository.findById(jobId).orElseThrow(() -> new DomainException(404,"JOB_NOT_FOUND","Không tìm thấy công việc"));
        if (!job.getEmployer().getId().equals(CurrentUser.id())) throw new DomainException(403,"NOT_JOB_OWNER","Bạn không sở hữu tin tuyển dụng này");
        return applicationRepository.findByJobIdOrderByCreatedAtDesc(jobId,JobServiceImpl.page(pageable));
    }
    @Override
    public Page<Application> getStudentApplications(Long studentId,Pageable pageable) {
        CurrentUser.requireRole(UserRole.STUDENT); CurrentUser.requireId(studentId);
        return applicationRepository.findByStudentIdOrderByCreatedAtDesc(CurrentUser.id(),JobServiceImpl.page(pageable));
    }
    @Override @Transactional
    public Application approveApplication(Long id,Long employerId) { return decide(id,employerId,ApplicationStatus.APPROVED); }
    @Override @Transactional
    public Application rejectApplication(Long id,Long employerId) { return decide(id,employerId,ApplicationStatus.REJECTED); }
    private Application decide(Long id,Long supplied,ApplicationStatus decision) {
        CurrentUser.requireRole(UserRole.EMPLOYER); CurrentUser.requireId(supplied);
        Long jobId=applicationRepository.findJobId(id).orElseThrow(() -> new DomainException(404,"APPLICATION_NOT_FOUND","Không tìm thấy đơn ứng tuyển"));
        Job job=jobRepository.findLockedById(jobId).orElseThrow(() -> new DomainException(404,"JOB_NOT_FOUND","Không tìm thấy công việc"));
        if (!job.getEmployer().getId().equals(CurrentUser.id())) throw new DomainException(403,"NOT_JOB_OWNER","Bạn không sở hữu tin tuyển dụng này");
        Application application=applicationRepository.findLockedById(id).orElseThrow(() -> new DomainException(404,"APPLICATION_NOT_FOUND","Không tìm thấy đơn ứng tuyển"));
        if (application.getStatus()!=ApplicationStatus.PENDING) throw new DomainException(409,"APPLICATION_ALREADY_DECIDED","Đơn đã được xử lý");
        if (job.getRemovedByAdmin() || job.getStatus()==JobStatus.COMPLETED || (decision==ApplicationStatus.APPROVED && job.getStatus()!=JobStatus.OPEN))
            throw new DomainException(409,"JOB_NOT_OPEN","Tin không còn cho phép xử lý đơn này");
        application.setStatus(decision); application.setDecidedAt(LocalDateTime.now());
        Application saved=applicationRepository.saveAndFlush(application);
        notificationService.createNotification(application.getStudent().getId(),"Đơn ứng tuyển "+job.getTitle()+(decision==ApplicationStatus.APPROVED?" đã được duyệt":" đã bị từ chối"),"APPLICATION",id);
        return saved;
    }
    @Override
    public boolean hasApplied(Long jobId,Long studentId) { return applicationRepository.countByJobIdAndStudentId(jobId,studentId)>0; }
    @Override
    public Application getByJobAndStudent(Long jobId,Long studentId) { return applicationRepository.findByJobIdAndStudentId(jobId,studentId).orElse(null); }
    @Override
    public ApplicationDTO toDto(Application application) {
        ApplicationDTO dto=new ApplicationDTO(application);
        dto.setReviewSubmitted(reviewRepository.existsByJobIdAndReviewerIdAndRevieweeId(application.getJob().getId(),application.getStudent().getId(),application.getJob().getEmployer().getId()));
        return dto;
    }
}
