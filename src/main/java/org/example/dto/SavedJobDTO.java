package org.example.dto;

import org.example.entity.Job;
import org.example.entity.JobStatus;
import org.example.entity.SavedJob;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record SavedJobDTO(Long id, LocalDateTime savedAt, boolean available, JobDTO job) {
    public static SavedJobDTO of(SavedJob saved) {
        Job job = saved.getJob();
        boolean open = job.getStatus() == JobStatus.OPEN && !job.getDeadline().isBefore(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        return new SavedJobDTO(saved.getId(), saved.getCreatedAt(), open, new JobDTO(job));
    }
}
