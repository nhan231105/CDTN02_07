package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Notification;
import org.example.entity.NotificationStatus;
import org.example.entity.User;
import org.example.repository.NotificationRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Notification createNotification(Long userId, String message, String relatedType, Long relatedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Nguoi dung khong ton tai"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created - ID: {}, User: {}, Type: {}", saved.getId(), userId, relatedType);
        return saved;
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thong bao khong ton tai"));
    }

    @Override
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = getNotificationById(notificationId);

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Ban khong co quyen xem thong bao nay");
        }

        notification.setStatus(NotificationStatus.READ);
        Notification updated = notificationRepository.save(notification);
        log.info("Notification {} marked as read by user {}", notificationId, userId);
        return updated;
    }

    @Override
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }
}
