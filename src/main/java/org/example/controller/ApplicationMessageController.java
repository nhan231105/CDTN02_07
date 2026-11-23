package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.example.service.ApplicationMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications/{id}/messages")
@RequiredArgsConstructor
public class ApplicationMessageController {
    private final ApplicationMessageService messages;

    @GetMapping
    public ApplicationMessageService.Conversation list(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page) {
        return messages.list(id, page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationMessageService.Message send(@PathVariable Long id, @Valid @RequestBody SendMessage request) {
        return messages.send(id, request.body());
    }

    @PatchMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@PathVariable Long id, @Valid @RequestBody ReadMessages request) {
        messages.read(id, request.through());
    }

    public record SendMessage(@NotBlank @Size(max = 2000) String body) {}
    public record ReadMessages(@Positive long through) {}
}
