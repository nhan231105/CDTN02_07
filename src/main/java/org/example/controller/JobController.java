package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.*;
import org.example.service.JobService;
import org.example.security.CurrentUser;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.example.entity.Job;
import org.example.entity.EmployerProfile;
import org.example.repository.EmployerProfileRepository;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    private final EmployerProfileRepository employers;

    private JobDTO jobDto(Job job) {
        return new JobDTO(job, employers.findById(job.getEmployer().getId()).orElse(null));
    }

    private Page<JobDTO> jobPage(Page<JobDTO> jobs) {
        var ids = jobs.stream().map(JobDTO::getEmployerId).distinct().toList();
        var profiles = employers.findAllById(ids).stream()
                .collect(Collectors.toMap(EmployerProfile::getUserId, Function.identity()));
        return jobs.map(job -> job.withEmployerProfile(profiles.get(job.getEmployerId())));
    }
    @PostMapping
    public ResponseEntity<JobDTO> create(@Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.status(201).body(jobDto(jobService.createJob(request,request.getEmployerId())));
    }
    @GetMapping({"","/search"})
    public Page<JobDTO> list(@RequestParam(required=false) String keyword,@RequestParam(required=false) String category,
            @RequestParam(required=false) String location,@RequestParam(required=false) BigDecimal minSalary,
            @RequestParam(required=false) BigDecimal maxSalary,@RequestParam(required=false) String workSchedule,
            @RequestParam(required=false) String jobType,@RequestParam(required=false) String payType,@RequestParam(required=false) String shift,
            @RequestParam(required=false) Boolean noExperience,@RequestParam(required=false) Boolean studentFriendly,
            @RequestParam(required=false) Integer maxHoursPerWeek,@RequestParam(required=false) String sortBy,
            @PageableDefault(size=12) Pageable pageable) {
        return jobPage(jobService.searchJobs(new JobSearchCriteria(keyword,category,location,minSalary,maxSalary,workSchedule,jobType,payType,shift,noExperience,studentFriendly,maxHoursPerWeek,sortBy),pageable).map(JobDTO::new));
    }
    @GetMapping("/nearby")
    public Page<JobDTO> nearby(@RequestParam double lat,@RequestParam double lng,@RequestParam(defaultValue="15") double radiusKm,@PageableDefault(size=12) Pageable pageable) {
        if (lat<-90||lat>90||lng<-180||lng>180) throw new org.example.common.DomainException(400,"INVALID_LOCATION","Tọa độ không hợp lệ");
        return jobPage(jobService.nearby(lat,lng,Math.min(Math.max(radiusKm,1),100),pageable));
    }
    @GetMapping("/{id}") public JobDTO get(@PathVariable Long id) { return jobDto(jobService.getJobById(id)); }
    @RequestMapping(value="/{id}",method={RequestMethod.PUT,RequestMethod.PATCH})
    public JobDTO update(@PathVariable Long id,@RequestParam(required=false) Long employerId,@Valid @RequestBody UpdateJobRequest request) {
        return jobDto(jobService.updateJob(id,employerId,request));
    }
    @PatchMapping("/{id}/status")
    public JobDTO status(@PathVariable Long id,@RequestParam(required=false) Long employerId,@Valid @RequestBody JobStatusUpdateRequest request) {
        return jobDto(jobService.updateJobStatus(id,employerId,request.getStatus().name()));
    }
    @GetMapping("/employer/{employerId}")
    public Page<JobDTO> own(@PathVariable Long employerId,@PageableDefault(size=12) Pageable pageable) {
        return jobPage(jobService.getEmployerJobs(employerId,pageable).map(JobDTO::new));
    }
}
