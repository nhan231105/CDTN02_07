package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.CreateApplicationRequest;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TalentService {
    static final int DAILY_INVITE_LIMIT = 10;
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Set<String> SHIFTS = Set.of("MORNING", "AFTERNOON", "EVENING", "WEEKEND", "FLEXIBLE");

    private final StudentProfileRepository students;
    private final EmployerProfileRepository employers;
    private final InvitationRepository invitations;
    private final JobRepository jobs;
    private final UserRepository users;
    private final ApplicationRepository applications;
    private final ApplicationService applicationService;
    private final NotificationService notifications;
    private final FileStorageService files;

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> search(String keyword, String shift, String area, int page) {
        CurrentUser.requireRole(UserRole.EMPLOYER);
        String s = shift == null || !SHIFTS.contains(shift) ? "" : shift;
        return students.searchTalent(clean(keyword), s, clean(area), PageRequest.of(Math.max(page, 0), 12))
                .map(p -> card(p, false));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(Long id) {
        CurrentUser.requireRole(UserRole.EMPLOYER);
        var m = card(visible(id), true);
        m.put("invitedJobIds", invitations.findByEmployerIdAndStudentId(CurrentUser.id(), id).stream()
                .map(i -> i.getJob().getId()).toList());
        return m;
    }

    @Transactional(readOnly = true)
    public StoredFile avatar(Long id) {
        CurrentUser.requireRole(UserRole.EMPLOYER);
        return files.requireImage(visible(id).getAvatarRef());
    }

    public Map<String, Object> invite(Long studentId, Long jobId, String message) {
        CurrentUser.requireRole(UserRole.EMPLOYER);
        Long employerId = CurrentUser.id();
        var profile = visible(studentId);
        var job = jobs.findById(jobId).orElseThrow(() -> new DomainException(404, "JOB_NOT_FOUND", "Không tìm thấy công việc"));
        if (!job.getEmployer().getId().equals(employerId))
            throw new DomainException(403, "NOT_JOB_OWNER", "Bạn không sở hữu tin tuyển dụng này");
        if (job.getRemovedByAdmin() || job.getStatus() != JobStatus.OPEN
                || job.getDeadline().isBefore(LocalDate.now(ZONE)))
            throw new DomainException(409, "JOB_NOT_OPEN", "Tin không còn nhận hồ sơ");
        if (invitations.existsByJobIdAndStudentId(jobId, studentId))
            throw new DomainException(409, "ALREADY_INVITED", "Bạn đã mời sinh viên này cho công việc này");
        if (applications.countByJobIdAndStudentId(jobId, studentId) > 0)
            throw new DomainException(409, "ALREADY_APPLIED", "Sinh viên đã ứng tuyển công việc này");
        if (invitations.countByEmployerIdAndCreatedAtAfter(employerId, LocalDate.now(ZONE).atStartOfDay()) >= DAILY_INVITE_LIMIT)
            throw new DomainException(429, "INVITE_LIMIT", "Mỗi ngày chỉ được gửi tối đa " + DAILY_INVITE_LIMIT + " lời mời");
        var invitation = new Invitation();
        invitation.setEmployer(job.getEmployer());
        invitation.setStudent(profile.getUser());
        invitation.setJob(job);
        invitation.setMessage(message == null || message.isBlank() ? null : message.trim());
        invitation = invitations.saveAndFlush(invitation);
        notifications.createNotification(studentId,
                orgName(employerId) + " mời bạn ứng tuyển: " + job.getTitle(), "INVITATION", invitation.getId());
        return Map.of("id", invitation.getId(), "status", invitation.getStatus().name());
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> sent(int page) {
        CurrentUser.requireRole(UserRole.EMPLOYER);
        return invitations.findByEmployerIdOrderByCreatedAtDesc(CurrentUser.id(), PageRequest.of(Math.max(page, 0), 20))
                .map(i -> {
                    Map<String, Object> m = base(i);
                    m.put("studentId", i.getStudent().getId());
                    m.put("studentName", i.getStudent().getFullName());
                    return m;
                });
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> mine() {
        CurrentUser.requireRole(UserRole.STUDENT);
        return invitations.findTop100ByStudentIdOrderByCreatedAtDesc(CurrentUser.id()).stream().map(i -> {
            Map<String, Object> m = base(i);
            m.put("employerId", i.getEmployer().getId());
            m.put("employerName", orgName(i.getEmployer().getId()));
            m.put("location", i.getJob().getLocation());
            return m;
        }).toList();
    }

    public Map<String, Object> respond(Long id, boolean accept) {
        CurrentUser.requireRole(UserRole.STUDENT);
        var invitation = invitations.findById(id).orElseThrow(() -> new DomainException(404, "INVITATION_NOT_FOUND", "Không tìm thấy lời mời"));
        if (!invitation.getStudent().getId().equals(CurrentUser.id()))
            throw new DomainException(403, "FORBIDDEN", "Lời mời không thuộc tài khoản của bạn");
        if (invitation.getStatus() != InvitationStatus.PENDING)
            throw new DomainException(409, "INVITATION_CLOSED", "Lời mời này đã được phản hồi");
        var job = invitation.getJob();
        if (accept) {
            var request = new CreateApplicationRequest();
            request.setConsentDataUsage(true);
            request.setCoverLetter("Nhận lời mời ứng tuyển từ nhà tuyển dụng.");
            if (applications.countByJobIdAndStudentId(job.getId(), CurrentUser.id()) == 0)
                applicationService.createApplication(job.getId(), CurrentUser.id(), request);
        }
        invitation.setStatus(accept ? InvitationStatus.ACCEPTED : InvitationStatus.DECLINED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitations.saveAndFlush(invitation);
        notifications.createNotification(invitation.getEmployer().getId(),
                invitation.getStudent().getFullName() + (accept ? " đã nhận lời mời" : " đã từ chối lời mời")
                        + " cho công việc: " + job.getTitle(), "INVITATION", invitation.getId());
        return base(invitation);
    }

    private StudentProfile visible(Long id) {
        var p = students.findById(id).orElseThrow(() -> new DomainException(404, "TALENT_NOT_FOUND", "Không tìm thấy ứng viên"));
        if (!p.isOpenToWork() || p.getUser().getStatus() != UserStatus.ACTIVE)
            throw new DomainException(404, "TALENT_NOT_FOUND", "Ứng viên hiện không công khai hồ sơ");
        return p;
    }

    private Map<String, Object> card(StudentProfile p, boolean full) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getUserId());
        m.put("name", p.getUser().getFullName());
        m.put("school", p.getSchool());
        m.put("major", p.getMajor());
        m.put("studyYear", p.getStudyYear());
        m.put("skills", split(p.getSkills()));
        m.put("availableShifts", split(p.getAvailableShifts()));
        m.put("preferredArea", p.getPreferredArea());
        m.put("hasAvatar", p.getAvatarRef() != null);
        String bio = p.getBio();
        m.put("bio", bio == null ? null : full || bio.length() <= 220 ? bio : bio.substring(0, 220) + "…");
        return m;
    }

    private Map<String, Object> base(Invitation i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("jobId", i.getJob().getId());
        m.put("jobTitle", i.getJob().getTitle());
        m.put("message", i.getMessage());
        m.put("status", i.getStatus().name());
        m.put("createdAt", i.getCreatedAt());
        m.put("respondedAt", i.getRespondedAt());
        return m;
    }

    private String orgName(Long employerId) {
        return employers.findById(employerId).map(EmployerProfile::getOrgName).filter(n -> n != null && !n.isBlank())
                .orElseGet(() -> users.findById(employerId).map(User::getFullName).orElse("Nhà tuyển dụng"));
    }

    private static List<String> split(String csv) {
        return csv == null || csv.isBlank() ? List.of() : Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replace("%", "").replace("_", "");
    }
}
