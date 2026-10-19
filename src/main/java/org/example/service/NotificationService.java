package org.example.service;

import org.example.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    
    Notification createNotification(Long userId, String message, String relatedType, Long relatedId);
    
    Notification getNotificationById(Long id);
    
    Page<Notification> getUserNotifications(Long userId, Pageable pageable);
    
    Notification markAsRead(Long notificationId, Long userId);
    
    long countUnreadNotifications(Long userId);
}
