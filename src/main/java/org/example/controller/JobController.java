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

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    @PostMapping
    public ResponseEntity<JobDTO> create(@Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.status(201).body(new JobDTO(jobService.createJob(request,request.getEmployerId())));
    }
    @GetMapping({"","/search"})
    public Page<JobDTO> list(@RequestParam(required=false) String keyword,@RequestParam(required=false) String category,
            @RequestParam(required=false) String location,@RequestParam(required=false) BigDecimal minSalary,
            @RequestParam(required=false) BigDecimal maxSalary,@RequestParam(required=false) String workSchedule,
            @PageableDefault(size=12) Pageable pageable) {
        return jobService.searchJobs(keyword,category,location,minSalary,maxSalary,workSchedule,pageable).map(JobDTO::new);
    }
    @GetMapping("/{id}") public JobDTO get(@PathVariable Long id) { return new JobDTO(jobService.getJobById(id)); }
    @RequestMapping(value="/{id}",method={RequestMethod.PUT,RequestMethod.PATCH})
    public JobDTO update(@PathVariable Long id,@RequestParam(required=false) Long employerId,@Valid @RequestBody UpdateJobRequest request) {
        return new JobDTO(jobService.updateJob(id,employerId,request));
    }
    @PatchMapping("/{id}/status")
    public JobDTO status(@PathVariable Long id,@RequestParam(required=false) Long employerId,@Valid @RequestBody JobStatusUpdateRequest request) {
        return new JobDTO(jobService.updateJobStatus(id,employerId,request.getStatus().name()));
    }
    @GetMapping("/employer/{employerId}")
    public Page<JobDTO> own(@PathVariable Long employerId,@PageableDefault(size=12) Pageable pageable) {
        return jobService.getEmployerJobs(employerId,pageable).map(JobDTO::new);
    }
}
