package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.entity.*;
import org.example.repository.ApplicationMessageRepository;
import org.example.security.CurrentUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationMessageService {
    private final ApplicationService applications;
    private final ApplicationMessageRepository messages;
    private final NotificationService notifications;

    public Conversation list(Long id, int page) {
        Application application = requireConversation(id);
        var slice = messages.findByApplicationIdOrderByIdDesc(id, PageRequest.of(Math.max(0, page), 30));
        User other = otherParticipant(application);
        return new Conversation(application.getJob().getTitle(), other.getFullName(),
                messages.countByApplicationIdAndRecipientIdAndReadAtIsNull(id, CurrentUser.id()),
                slice.hasNext(), slice.getContent().stream().map(ApplicationMessageService::view).toList());
    }

    @Transactional
    public Message send(Long id, String body) {
        Application application = requireConversation(id);
        String text = body.strip();
        if (text.isBlank()) throw new DomainException(400, "MESSAGE_REQUIRED", "Vui lòng nhập tin nhắn.");
        User other = otherParticipant(application);
        ApplicationMessage message = new ApplicationMessage();
        message.setApplicationId(id);
        message.setSenderId(CurrentUser.id());
        message.setRecipientId(other.getId());
        message.setBody(text);
        message.setSentAt(Instant.now());
        messages.saveAndFlush(message);
        notifications.createNotification(other.getId(),
                "Bạn có tin nhắn mới về vị trí " + application.getJob().getTitle(), "MESSAGE", id);
        return view(message);
    }

    @Transactional
    public void read(Long id, long through) {
        requireConversation(id);
        messages.markRead(id, CurrentUser.id(), through, Instant.now());
    }

    private Application requireConversation(Long id) {
        Application application = applications.getApplicationById(id);
        boolean student = application.getStudent().getId().equals(CurrentUser.id());
        CurrentUser.requireRole(student ? UserRole.STUDENT : UserRole.EMPLOYER);
        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new DomainException(403, "CONVERSATION_UNAVAILABLE", "Hội thoại mở sau khi ứng viên được nhận.");
        }
        return application;
    }

    private User otherParticipant(Application application) {
        return application.getStudent().getId().equals(CurrentUser.id())
                ? application.getJob().getEmployer() : application.getStudent();
    }

    private static Message view(ApplicationMessage message) {
        return new Message(message.getId(), message.getSenderId().equals(CurrentUser.id()),
                message.getBody(), message.getSentAt(), message.getReadAt());
    }

    public record Conversation(String jobTitle, String participant, long unread, boolean hasOlder, List<Message> messages) {}
    public record Message(Long id, boolean mine, String body, Instant sentAt, Instant readAt) {}
}
