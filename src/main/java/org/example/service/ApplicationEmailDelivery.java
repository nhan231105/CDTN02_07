package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
class ApplicationEmailDelivery {
    private final CampusEmail email;

    record Notice(String eventKey, String recipient, String subject, String body) {}

    @TransactionalEventListener
    public void deliver(Notice notice) {
        String key = "campuswork:" + notice.eventKey() + ":" + notice.recipient();
        UUID idempotencyKey = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
        try {
            email.sendMessage(notice.recipient(), notice.subject(), notice.body(), idempotencyKey);
        } catch (CampusEmail.DeliveryException failure) {
            log.warn("CampusWork application email failed for event {}", notice.eventKey());
        }
    }
}
