package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.dto.CreateJobRequest;
import org.example.dto.JobDTO;
import org.example.dto.JobStatusUpdateRequest;
import org.example.dto.UpdateJobRequest;
import org.example.entity.Job;
import org.example.service.JobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/jobs")
@AllArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobDTO> createJob(@Valid @RequestBody CreateJobRequest request) {
        Job job = jobService.createJob(request, request.getEmployerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JobDTO(job));
    }

    @GetMapping
    public ResponseEntity<Page<JobDTO>> getAllJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        // Nếu có filter thì search, không có thì lấy tất cả
        Page<Job> jobs;
        if (keyword != null || category != null || location != null) {
            jobs = jobService.searchJobs(keyword, category, location, pageable);
        } else {
            jobs = jobService.getAllJobs(pageable);
        }
        Page<JobDTO> dtos = jobs.map(JobDTO::new);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        return ResponseEntity.ok(new JobDTO(job));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<JobDTO> updateJob(
            @PathVariable Long id,
            @RequestParam Long employerId,
            @Valid @RequestBody UpdateJobRequest request) {
        Job job = jobService.updateJob(id, employerId, request);
        return ResponseEntity.ok(new JobDTO(job));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobDTO> updateJobStatus(
            @PathVariable Long id,
            @RequestParam Long employerId,
            @Valid @RequestBody JobStatusUpdateRequest request) {
        Job job = jobService.updateJobStatus(id, employerId, request.getStatus().name());
        return ResponseEntity.ok(new JobDTO(job));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<JobDTO>> searchJobsLegacy(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        Page<Job> jobs = jobService.searchJobs(keyword, category, location, pageable);
        Page<JobDTO> dtos = jobs.map(JobDTO::new);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/employer/{employerId}")
    public ResponseEntity<Page<JobDTO>> getEmployerJobs(
            @PathVariable Long employerId,
            Pageable pageable) {
        Page<Job> jobs = jobService.getEmployerJobs(employerId, pageable);
        Page<JobDTO> dtos = jobs.map(JobDTO::new);
        return ResponseEntity.ok(dtos);
    }
}
