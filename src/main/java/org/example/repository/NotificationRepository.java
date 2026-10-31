package org.example.repository;

import org.example.entity.Notification;
import org.example.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    @Query("select n from Notification n where n.user.id = :userId order by case when n.status = org.example.entity.NotificationStatus.UNREAD then 0 else 1 end, n.createdAt desc, n.id desc")
    Page<Notification> findInbox(@Param("userId") Long userId, Pageable pageable);
    long countByUserIdAndStatus(Long userId, NotificationStatus status);
    void deleteByUserId(Long userId);
}
