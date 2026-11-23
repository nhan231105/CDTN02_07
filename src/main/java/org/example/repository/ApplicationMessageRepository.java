package org.example.repository;

import org.example.entity.ApplicationMessage;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.time.Instant;

public interface ApplicationMessageRepository extends JpaRepository<ApplicationMessage, Long> {
    Slice<ApplicationMessage> findByApplicationIdOrderByIdDesc(Long applicationId, Pageable pageable);

    long countByApplicationIdAndRecipientIdAndReadAtIsNull(Long applicationId, Long recipientId);

    @Modifying
    @Query("""
            update ApplicationMessage m set m.readAt = :now
            where m.applicationId = :applicationId and m.recipientId = :recipientId
              and m.id <= :through and m.readAt is null
            """)
    int markRead(Long applicationId, Long recipientId, long through, Instant now);
}
