package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.service.FileStorageService;
import org.example.service.TalentService;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TalentController {
    private final TalentService talent;
    private final FileStorageService files;

    public record InviteRequest(@NotNull Long studentId, @NotNull Long jobId, @Size(max = 500) String message) { }
    public record RespondRequest(@NotNull Boolean accept) { }

    @GetMapping("/talent")
    public Page<Map<String, Object>> search(@RequestParam(defaultValue = "") String keyword,
                                            @RequestParam(defaultValue = "") String shift,
                                            @RequestParam(defaultValue = "") String area,
                                            @RequestParam(defaultValue = "0") int page) {
        return talent.search(keyword, shift, area, page);
    }

    @GetMapping("/talent/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return talent.detail(id);
    }

    @GetMapping("/talent/{id}/avatar")
    public ResponseEntity<byte[]> avatar(@PathVariable Long id) {
        var file = talent.avatar(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType()))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(files.read(file));
    }

    @PostMapping("/invitations")
    public ResponseEntity<Map<String, Object>> invite(@Valid @RequestBody InviteRequest request) {
        return ResponseEntity.status(201).body(talent.invite(request.studentId(), request.jobId(), request.message()));
    }

    @GetMapping("/invitations/sent")
    public Page<Map<String, Object>> sent(@RequestParam(defaultValue = "0") int page) {
        return talent.sent(page);
    }

    @GetMapping("/me/invitations")
    public List<Map<String, Object>> mine() {
        return talent.mine();
    }

    @PatchMapping("/invitations/{id}/respond")
    public Map<String, Object> respond(@PathVariable Long id, @Valid @RequestBody RespondRequest request) {
        return talent.respond(id, request.accept());
    }
}
