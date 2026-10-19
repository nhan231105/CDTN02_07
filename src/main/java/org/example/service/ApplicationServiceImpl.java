package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreateApplicationRequest;
import org.example.entity.Application;
import org.example.entity.ApplicationStatus;
import org.example.entity.Job;
import org.example.entity.JobStatus;
import org.example.entity.User;
import org.example.repository.ApplicationRepository;
import org.example.repository.JobRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Application createApplication(Long jobId, Long studentId, CreateApplicationRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai"));

        if (!job.getStatus().equals(JobStatus.OPEN)) {
            throw new RuntimeException("Cong viec khong nhan don ung tuyen");
        }

        if (hasApplied(jobId, studentId)) {
            throw new RuntimeException("Ban da ung tuyen cho cong viec nay roi");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Nguoi dung khong ton tai"));

        Application app = new Application();
        app.setJob(job);
        app.setStudent(student);
        app.setFullName(request.getFullName());
        app.setEmail(request.getEmail());
        app.setPhone(request.getPhone());
        app.setCoverLetter(request.getCoverLetter());
        app.setConsentDataUsage(request.getConsentDataUsage());
        app.setCvFileRef(request.getCvFileRef());
        app.setStatus(ApplicationStatus.PENDING);

        Application savedApp = applicationRepository.save(app);
        log.info("Application created - ID: {}, Job: {}, Student: {}", savedApp.getId(), jobId, studentId);

        notificationService.createNotification(job.getEmployer().getId(), 
            "Ban co don ung tuyen moi cho cong viec: " + job.getTitle(), 
            "APPLICATION", savedApp.getId());

        return savedApp;
    }

    @Override
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don ung tuyen khong ton tai"));
    }

    @Override
    public Page<Application> getJobApplications(Long jobId, Long employerId, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Cong viec khong ton tai"));

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Ban khong co quyen xem don ung tuyen cho cong viec nay");
        }

        return applicationRepository.findByJobIdOrderByCreatedAtDesc(jobId, pageable);
    }

    @Override
    public Page<Application> getStudentApplications(Long studentId, Pageable pageable) {
        return applicationRepository.findByStudentIdOrderByCreatedAtDesc(studentId, pageable);
    }

    @Override
    @Transactional
    public Application approveApplication(Long applicationId, Long employerId) {
        Application app = getApplicationById(applicationId);

        if (!app.getJob().getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Ban khong co quyen phe duyet don ung tuyen nay");
        }

        if (!app.getStatus().equals(ApplicationStatus.PENDING)) {
            throw new RuntimeException("Chi co the phe duyet don ung tuyen o trang thai CHO XU LY");
        }

        app.setStatus(ApplicationStatus.APPROVED);
        app.setDecidedAt(LocalDateTime.now());

        Application updated = applicationRepository.save(app);
        log.info("Application {} approved by employer {}", applicationId, employerId);

        notificationService.createNotification(app.getStudent().getId(),
            "Don ung tuyen cua ban cho cong viec: " + app.getJob().getTitle() + " da duoc chap nhan",
            "APPLICATION", applicationId);

        return updated;
    }

    @Override
    @Transactional
    public Application rejectApplication(Long applicationId, Long employerId) {
        Application app = getApplicationById(applicationId);

        if (!app.getJob().getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Ban khong co quyen tu choi don ung tuyen nay");
        }

        if (!app.getStatus().equals(ApplicationStatus.PENDING)) {
            throw new RuntimeException("Chi co the tu choi don ung tuyen o trang thai CHO XU LY");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setDecidedAt(LocalDateTime.now());

        Application updated = applicationRepository.save(app);
        log.info("Application {} rejected by employer {}", applicationId, employerId);

        notificationService.createNotification(app.getStudent().getId(),
            "Don ung tuyen cua ban cho cong viec: " + app.getJob().getTitle() + " da bi tu choi",
            "APPLICATION", applicationId);

        return updated;
    }

    @Override
    public boolean hasApplied(Long jobId, Long studentId) {
        return applicationRepository.countByJobIdAndStudentId(jobId, studentId) > 0;
    }

    @Override
    public Application getByJobAndStudent(Long jobId, Long studentId) {
        return applicationRepository.findByJobIdAndStudentId(jobId, studentId)
                .orElse(null);
    }
}
