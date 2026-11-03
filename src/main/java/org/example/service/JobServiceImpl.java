package org.example.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.CreateJobRequest;
import org.example.dto.JobSearchCriteria;
import org.example.dto.UpdateJobRequest;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.CurrentUser;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private static LocalDate today() { return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")); }
    public static Pageable page(Pageable supplied) {
        return page(supplied,Sort.by(Sort.Direction.DESC,"createdAt","id"));
    }
    public static Pageable page(Pageable supplied,Sort sort) {
        return PageRequest.of(Math.max(0,supplied.getPageNumber()), Math.max(1,Math.min(12,supplied.getPageSize())), sort);
    }
    private Job job(Long id) { return jobRepository.findById(id).orElseThrow(() -> new DomainException(404,"JOB_NOT_FOUND","Không tìm thấy công việc")); }
    private Job locked(Long id) { return jobRepository.findLockedById(id).orElseThrow(() -> new DomainException(404,"JOB_NOT_FOUND","Không tìm thấy công việc")); }
    private void owner(Job job,Long suppliedId) {
        CurrentUser.requireRole(UserRole.EMPLOYER);
        CurrentUser.requireId(suppliedId);
        if (!job.getEmployer().getId().equals(CurrentUser.id())) throw new DomainException(403,"NOT_JOB_OWNER","Bạn không sở hữu tin tuyển dụng này");
    }
    @Override @Transactional
    public Job createJob(CreateJobRequest request,Long employerId) {
        CurrentUser.requireRole(UserRole.EMPLOYER);
        CurrentUser.requireId(employerId);
        User employer=userRepository.findById(CurrentUser.id()).orElseThrow(() -> new DomainException(401,"UNAUTHENTICATED","Vui lòng đăng nhập"));
        Job job=new Job();
        job.setEmployer(employer); job.setTitle(normalized(request.getTitle(),5,200,"Tiêu đề")); job.setDescription(normalized(request.getDescription(),20,5000,"Mô tả"));
        job.setRequirements(request.getRequirements()); job.setBenefits(request.getBenefits()); job.setCategory(normalized(request.getCategory(),1,100,"Ngành nghề"));
        job.setLocation(normalized(request.getLocation(),1,100,"Địa điểm")); job.setWorkSchedule(normalized(request.getWorkSchedule(),1,200,"Lịch làm việc"));
        job.setSalary(request.getSalary()); job.setDeadline(request.getDeadline()); job.setStatus(JobStatus.OPEN);
        if (request.getLat()!=null && request.getLng()!=null) { job.setLat(request.getLat()); job.setLng(request.getLng()); }
        applyPartTime(job,request.getJobType(),request.getPayType(),request.getShifts(),request.getHoursPerWeek(),request.getNoExperienceRequired(),request.getStudentFriendly(),request.getAddress(),request.getStartDate(),request.getDurationMonths(),request.getHeadcount());
        return jobRepository.saveAndFlush(job);
    }
    @Override
    public Job getJobById(Long id) {
        Job found=job(id);
        if (Boolean.TRUE.equals(found.getRemovedByAdmin())) throw new DomainException(404,"JOB_NOT_FOUND","Tin tuyển dụng không còn công khai");
        if (found.getStatus()==JobStatus.OPEN && !found.getDeadline().isBefore(today())) return found;
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if (auth!=null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Long current=CurrentUser.id();
            if (found.getEmployer().getId().equals(current) || applicationRepository.countByJobIdAndStudentId(id,current)>0) return found;
        }
        throw new DomainException(404,"JOB_NOT_FOUND","Tin tuyển dụng không còn công khai");
    }
    @Override
    public Page<Job> searchJobs(String keyword,String category,String location,Pageable pageable) {
        return searchJobs(keyword,category,location,null,null,null,pageable);
    }
    @Override
    public Page<Job> searchJobs(String keyword,String category,String location,BigDecimal minSalary,BigDecimal maxSalary,String schedule,Pageable pageable) {
        return searchJobs(JobSearchCriteria.basic(keyword,category,location,minSalary,maxSalary,schedule),pageable);
    }
    @Override
    public Page<Job> searchJobs(JobSearchCriteria c,Pageable pageable) {
        BigDecimal minSalary=c.minSalary(),maxSalary=c.maxSalary();
        if ((minSalary!=null && minSalary.signum()<0) || (maxSalary!=null && maxSalary.signum()<0) || (minSalary!=null && maxSalary!=null && minSalary.compareTo(maxSalary)>0))
            throw new DomainException(400,"INVALID_SALARY_RANGE","Khoảng lương không hợp lệ");
        JobType type=has(c.jobType())?parseEnum(JobType.class,c.jobType(),"Loại công việc"):null;
        PayType pay=has(c.payType())?parseEnum(PayType.class,c.payType(),"Hình thức trả lương"):null;
        JobShift shift=has(c.shift())?parseEnum(JobShift.class,c.shift(),"Ca làm việc"):null;
        if (c.maxHoursPerWeek()!=null && c.maxHoursPerWeek()<1) throw new DomainException(400,"INVALID_HOURS","Số giờ mỗi tuần không hợp lệ");
        return jobRepository.findAll((root,query,builder) -> {
            List<Predicate> predicates=new ArrayList<>();
            predicates.add(builder.isFalse(root.get("removedByAdmin")));
            predicates.add(builder.equal(root.get("status"),JobStatus.OPEN));
            predicates.add(builder.greaterThanOrEqualTo(root.get("deadline"),today()));
            if (has(c.keyword())) {
                String pattern=like(c.keyword());
                predicates.add(builder.or(builder.like(builder.lower(root.get("title")),pattern,'!'),builder.like(builder.lower(root.get("description")),pattern,'!')));
            }
            if (has(c.category())) predicates.add(builder.equal(builder.lower(root.get("category")),c.category().trim().toLowerCase(Locale.ROOT)));
            if (has(c.location())) predicates.add(builder.like(builder.lower(root.get("location")),like(c.location()),'!'));
            if (has(c.workSchedule())) predicates.add(builder.like(builder.lower(root.get("workSchedule")),like(c.workSchedule()),'!'));
            if (minSalary!=null) predicates.add(builder.greaterThanOrEqualTo(root.<BigDecimal>get("salary"),minSalary));
            if (maxSalary!=null) predicates.add(builder.lessThanOrEqualTo(root.<BigDecimal>get("salary"),maxSalary));
            if (type!=null) predicates.add(builder.equal(root.get("jobType"),type));
            if (pay!=null) predicates.add(builder.equal(root.get("payType"),pay));
            if (shift!=null) predicates.add(builder.or(builder.like(root.<String>get("shifts"),"%"+shift.name()+"%"),builder.like(root.<String>get("shifts"),"%"+JobShift.FLEXIBLE.name()+"%")));
            if (Boolean.TRUE.equals(c.noExperience())) predicates.add(builder.isTrue(root.get("noExperienceRequired")));
            if (Boolean.TRUE.equals(c.studentFriendly())) predicates.add(builder.isTrue(root.get("studentFriendly")));
            if (c.maxHoursPerWeek()!=null) predicates.add(builder.and(builder.isNotNull(root.get("hoursPerWeek")),builder.lessThanOrEqualTo(root.<Integer>get("hoursPerWeek"),c.maxHoursPerWeek())));
            return builder.and(predicates.toArray(Predicate[]::new));
        },page(pageable,sortFor(c.sortBy())));
    }
    private static Sort sortFor(String sortBy) {
        String key=sortBy==null?"":sortBy.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "salary_desc" -> Sort.by(Sort.Order.desc("salary"),Sort.Order.desc("createdAt"),Sort.Order.desc("id"));
            case "salary_asc" -> Sort.by(Sort.Order.asc("salary"),Sort.Order.desc("createdAt"),Sort.Order.desc("id"));
            case "deadline" -> Sort.by(Sort.Order.asc("deadline"),Sort.Order.desc("id"));
            default -> Sort.by(Sort.Direction.DESC,"createdAt","id");
        };
    }
    private static <E extends Enum<E>> E parseEnum(Class<E> type,String value,String field) {
        try { return Enum.valueOf(type,value.trim().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { throw new DomainException(400,"INVALID_JOB_FIELD",field+" không hợp lệ"); }
    }
    private static String joinShifts(List<String> shifts) {
        if (shifts==null) return null;
        return shifts.stream().filter(s -> s!=null && !s.isBlank()).map(s -> parseEnum(JobShift.class,s,"Ca làm việc").name()).distinct().collect(java.util.stream.Collectors.joining(","));
    }
    private static String trimToNull(String value) { return value==null||value.isBlank()?null:value.trim(); }
    private static void applyPartTime(Job job,String jobType,String payType,List<String> shifts,Integer hoursPerWeek,Boolean noExperience,Boolean studentFriendly,String address,LocalDate startDate,Integer duration,Integer headcount) {
        if (jobType!=null) job.setJobType(parseEnum(JobType.class,jobType,"Loại công việc"));
        if (payType!=null) job.setPayType(parseEnum(PayType.class,payType,"Hình thức trả lương"));
        if (shifts!=null) job.setShifts(joinShifts(shifts));
        if (hoursPerWeek!=null) job.setHoursPerWeek(hoursPerWeek);
        if (noExperience!=null) job.setNoExperienceRequired(noExperience);
        if (studentFriendly!=null) job.setStudentFriendly(studentFriendly);
        if (address!=null) job.setAddress(trimToNull(address));
        if (startDate!=null) job.setStartDate(startDate);
        if (duration!=null) job.setDurationMonths(duration);
        if (headcount!=null) job.setHeadcount(headcount);
    }

    private static String normalized(String value,int min,int max,String field) {
        String normalized=value==null?"":value.trim();
        if (normalized.length()<min || normalized.length()>max)
            throw new DomainException(400,"INVALID_JOB_FIELD",field+" phải từ "+min+" đến "+max+" ký tự sau khi bỏ khoảng trắng ở hai đầu");
        return normalized;
    }
    private static boolean has(String text) { return text!=null && !text.isBlank(); }
    private static String like(String text) { return "%"+text.trim().toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%"; }
    @Override @Transactional(readOnly=true)
    public Page<org.example.dto.JobDTO> nearby(double lat,double lng,double radiusKm,Pageable pageable) {
        double dLat=radiusKm/111.0, dLng=radiusKm/(111.0*Math.max(0.2,Math.cos(Math.toRadians(lat))));
        var list=new java.util.ArrayList<org.example.dto.JobDTO>();
        for (Job j:jobRepository.findOpenInBox(today(),lat-dLat,lat+dLat,lng-dLng,lng+dLng)) {
            double d=haversine(lat,lng,j.getLat(),j.getLng());
            if (d>radiusKm) continue;
            var dto=new org.example.dto.JobDTO(j); dto.setDistanceKm(Math.round(d*10)/10.0); list.add(dto);
        }
        list.sort(java.util.Comparator.comparing(org.example.dto.JobDTO::getDistanceKm).thenComparing(org.example.dto.JobDTO::getId));
        int from=(int)Math.min(pageable.getOffset(),list.size()), to=Math.min(from+pageable.getPageSize(),list.size());
        return new org.springframework.data.domain.PageImpl<>(list.subList(from,to),pageable,list.size());
    }
    private static double haversine(double la1,double lo1,double la2,double lo2) {
        double dLa=Math.toRadians(la2-la1), dLo=Math.toRadians(lo2-lo1);
        double a=Math.sin(dLa/2)*Math.sin(dLa/2)+Math.cos(Math.toRadians(la1))*Math.cos(Math.toRadians(la2))*Math.sin(dLo/2)*Math.sin(dLo/2);
        return 6371*2*Math.asin(Math.sqrt(a));
    }
    @Override
    public Page<Job> getEmployerJobs(Long employerId,Pageable pageable) {
        CurrentUser.requireRole(UserRole.EMPLOYER); CurrentUser.requireId(employerId);
        return jobRepository.findByEmployerIdAndRemovedByAdminFalse(CurrentUser.id(),page(pageable));
    }
    @Override @Transactional
    public Job updateJob(Long id,Long employerId,UpdateJobRequest request) {
        Job found=locked(id); owner(found,employerId);
        if (found.getRemovedByAdmin() || found.getStatus()!=JobStatus.OPEN || applicationRepository.countByJobId(id)>0)
            throw new DomainException(409,"JOB_NOT_EDITABLE","Chỉ được sửa tin đang tuyển và chưa có ứng viên");
        if (request.getTitle()!=null) found.setTitle(normalized(request.getTitle(),5,200,"Tiêu đề"));
        if (request.getDescription()!=null) found.setDescription(normalized(request.getDescription(),20,5000,"Mô tả"));
        if (request.getRequirements()!=null) found.setRequirements(request.getRequirements());
        if (request.getBenefits()!=null) found.setBenefits(request.getBenefits());
        if (request.getCategory()!=null) found.setCategory(normalized(request.getCategory(),1,100,"Ngành nghề"));
        if (request.getLocation()!=null) found.setLocation(normalized(request.getLocation(),1,100,"Địa điểm"));
        if (request.getWorkSchedule()!=null) found.setWorkSchedule(normalized(request.getWorkSchedule(),1,200,"Lịch làm việc"));
        if (request.getSalary()!=null) found.setSalary(request.getSalary());
        if (request.getDeadline()!=null) found.setDeadline(request.getDeadline());
        if (request.getLat()!=null && request.getLng()!=null) { found.setLat(request.getLat()); found.setLng(request.getLng()); }
        applyPartTime(found,request.getJobType(),request.getPayType(),request.getShifts(),request.getHoursPerWeek(),request.getNoExperienceRequired(),request.getStudentFriendly(),request.getAddress(),request.getStartDate(),request.getDurationMonths(),request.getHeadcount());
        return jobRepository.saveAndFlush(found);
    }
    @Override @Transactional
    public Job updateJobStatus(Long id,Long employerId,String newStatus) {
        Job found=locked(id); owner(found,employerId);
        JobStatus next;
        try { next=JobStatus.valueOf(newStatus.toUpperCase(Locale.ROOT)); } catch (Exception e) { throw new DomainException(400,"INVALID_STATUS","Trạng thái không hợp lệ"); }
        if (found.getRemovedByAdmin() || !((found.getStatus()==JobStatus.OPEN && next==JobStatus.FILLED) || (found.getStatus()==JobStatus.FILLED && next==JobStatus.COMPLETED)))
            throw new DomainException(409,"INVALID_JOB_TRANSITION","Chuyển trạng thái không hợp lệ");
        if (next==JobStatus.FILLED && applicationRepository.countByJobIdAndStatus(id,ApplicationStatus.APPROVED)==0)
            throw new DomainException(409,"NO_APPROVED_APPLICANT","Cần duyệt ít nhất một ứng viên trước khi đóng tuyển");
        found.setStatus(next); return jobRepository.saveAndFlush(found);
    }
    @Override @Transactional
    public void removeJob(Long id,Long adminId,String reason) {
        CurrentUser.requireRole(UserRole.ADMIN); CurrentUser.requireId(adminId);
        if (reason==null || reason.isBlank()) throw new DomainException(400,"REASON_REQUIRED","Cần cung cấp lý do");
        Job found=locked(id); found.setRemovedByAdmin(true); found.setRemovalReason(reason); jobRepository.save(found);
    }
    @Override public Page<Job> getAllJobs(Pageable pageable) { return searchJobs(null,null,null,pageable); }
    @Override public List<Job> getJobsNeedingReviews() {
        CurrentUser.requireRole(UserRole.EMPLOYER);
        return jobRepository.findByEmployerIdAndRemovedByAdminFalse(CurrentUser.id()).stream().filter(j -> j.getStatus()==JobStatus.COMPLETED).toList();
    }
}
