package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.SavedJobDTO;
import org.example.service.SavedJobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {
    private final SavedJobService service;

    @GetMapping
    public Page<SavedJobDTO> list(@PageableDefault(size = 12) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/ids")
    public List<Long> ids() {
        return service.ids();
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<Void> save(@PathVariable Long jobId) {
        service.save(jobId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> remove(@PathVariable Long jobId) {
        service.remove(jobId);
        return ResponseEntity.noContent().build();
    }
}
