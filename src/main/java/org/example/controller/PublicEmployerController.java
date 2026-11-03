package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.JobDTO;
import org.example.entity.UserRole;
import org.example.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class PublicEmployerController {
    private final UserRepository users;
    private final EmployerProfileRepository employers;
    private final ReviewRepository reviews;
    private final JobRepository jobs;
    private final org.example.service.FileStorageService files;

    @GetMapping("/employers/{id}/logo")
    @Transactional(readOnly = true)
    public org.springframework.http.ResponseEntity<byte[]> logo(@PathVariable Long id) {
        return image(employers.findById(id).map(p -> p.getLogoRef()).orElse(null));
    }

    @GetMapping("/employers/{id}/cover")
    @Transactional(readOnly = true)
    public org.springframework.http.ResponseEntity<byte[]> cover(@PathVariable Long id) {
        return image(employers.findById(id).map(p -> p.getCoverRef()).orElse(null));
    }

    private org.springframework.http.ResponseEntity<byte[]> image(String ref) {
        var file = files.requireImage(ref);
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(file.getContentType()))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofDays(30)).cachePublic())
                .body(files.read(file));
    }

    @GetMapping("/employers/{id}/public")
    @Transactional(readOnly = true)
    public Map<String, Object> publicProfile(@PathVariable Long id) {
        var user = users.findById(id).filter(u -> u.getRole() == UserRole.EMPLOYER)
                .orElseThrow(() -> new DomainException(404, "EMPLOYER_NOT_FOUND", "Không tìm thấy nhà tuyển dụng"));
        var profile = employers.findById(id).orElse(null);
        Double avg = reviews.averageRating(id);
        var open = jobs.findOpenByEmployer(id, LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), PageRequest.of(0, 20));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", profile != null && profile.getOrgName() != null && !profile.getOrgName().isBlank() ? profile.getOrgName() : user.getFullName());
        if (profile != null) {
            m.put("address", profile.getAddress());
            m.put("description", profile.getDescription());
            m.put("logoRef", profile.getLogoRef());
            m.put("website", profile.getWebsite());
            m.put("industry", profile.getIndustry());
            m.put("companySize", profile.getCompanySize());
            m.put("coverRef", profile.getCoverRef());
            m.put("tagline", profile.getTagline());
            m.put("foundedYear", profile.getFoundedYear());
            m.put("benefits", org.example.service.MeProfileService.benefits(profile.getBenefits()));
        }
        m.put("email", user.getEmail());
        m.put("averageRating", avg == null ? null : Math.round(avg * 10) / 10.0);
        m.put("reviewCount", reviews.countByRevieweeIdAndIsHiddenFalse(id));
        m.put("openJobCount", open.getTotalElements());
        m.put("openJobs", open.getContent().stream().map(JobDTO::new).toList());
        return m;
    }
}
