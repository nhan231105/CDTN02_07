package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.CurrentUser;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    @Override @Transactional
    public Notification createNotification(Long userId,String message,String relatedType,Long relatedId) {
        User user=userRepository.findById(userId).orElseThrow(() -> new DomainException(404,"USER_NOT_FOUND","Không tìm thấy người nhận"));
        Notification notification=new Notification(); notification.setUser(user); notification.setMessage(message);
        notification.setRelatedType(relatedType); notification.setRelatedId(relatedId); notification.setStatus(NotificationStatus.UNREAD);
        return notificationRepository.saveAndFlush(notification);
    }
    @Override
    public Notification getNotificationById(Long id) {
        Notification notification=notificationRepository.findById(id).orElseThrow(() -> new DomainException(404,"NOTIFICATION_NOT_FOUND","Không tìm thấy thông báo"));
        if (!notification.getUser().getId().equals(CurrentUser.id())) throw new DomainException(403,"NOTIFICATION_FORBIDDEN","Bạn không sở hữu thông báo này");
        return notification;
    }
    @Override
    public Page<Notification> getUserNotifications(Long userId,Pageable pageable) {
        CurrentUser.requireId(userId);
        return notificationRepository.findInbox(CurrentUser.id(),PageRequest.of(Math.max(0,pageable.getPageNumber()),Math.max(1,Math.min(50,pageable.getPageSize()))));
    }
    @Override @Transactional
    public Notification markAsRead(Long id,Long userId) {
        CurrentUser.requireId(userId);
        Notification notification=getNotificationById(id); notification.setStatus(NotificationStatus.READ);
        return notificationRepository.saveAndFlush(notification);
    }
    @Override public long countUnreadNotifications(Long userId) {
        CurrentUser.requireId(userId);
        return notificationRepository.countByUserIdAndStatus(CurrentUser.id(),NotificationStatus.UNREAD);
    }
}
