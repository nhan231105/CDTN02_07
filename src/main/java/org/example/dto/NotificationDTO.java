package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Notification;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String message;
    private String status;
    private String relatedType;
    private Long relatedId;
    private LocalDateTime createdAt;

    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.userId = notification.getUser().getId();
        this.message = notification.getMessage();
        this.status = notification.getStatus().name();
        this.relatedType = notification.getRelatedType();
        this.relatedId = notification.getRelatedId();
        this.createdAt = notification.getCreatedAt();
    }
}
