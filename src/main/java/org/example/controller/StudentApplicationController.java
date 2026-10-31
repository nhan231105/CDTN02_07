package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.ApplicationDTO;
import org.example.dto.CreateReviewRequest;
import org.example.dto.ReviewDTO;
import org.example.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.example.security.CurrentUser;
import org.example.service.ApplicationService;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudentApplicationController {
    private final ApplicationService applicationService;
    private final ReviewService reviewService;
    @GetMapping("/me/applications")
    public Page<ApplicationDTO> applications(@PageableDefault(size=12) Pageable pageable) {
        return applicationService.getStudentApplications(CurrentUser.id(),pageable).map(applicationService::toDto);
    }
    @PostMapping("/applications/{id}/reviews")
    public ResponseEntity<ReviewDTO> review(@PathVariable Long id,@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(201).body(new ReviewDTO(reviewService.createApplicationReview(id,request)));
    }
    @GetMapping("/applications/{id}")
    public ApplicationDTO application(@PathVariable Long id) { return applicationService.toDto(applicationService.getApplicationById(id)); }
}
