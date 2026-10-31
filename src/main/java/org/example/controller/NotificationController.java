package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.dto.NotificationDTO;
import org.example.entity.Notification;
import org.example.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/me/notifications", "/notifications"})
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getUserNotifications(
            @RequestParam Long userId,
            Pageable pageable) {
        Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
        Page<NotificationDTO> dtos = notifications.map(NotificationDTO::new);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long notificationId) {
        Notification notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(new NotificationDTO(notification));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        Notification notification = notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(new NotificationDTO(notification));
    }

    @GetMapping("/count/unread")
    public ResponseEntity<Long> countUnreadNotifications(@RequestParam Long userId) {
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }
}
