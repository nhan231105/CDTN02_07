CREATE TABLE IF NOT EXISTS application_messages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    body VARCHAR(2000) NOT NULL,
    sent_at DATETIME(6) NOT NULL,
    read_at DATETIME(6),
    INDEX idx_application_messages_history (application_id, id),
    INDEX idx_application_messages_unread (application_id, recipient_id, read_at),
    FOREIGN KEY (application_id) REFERENCES applications(id),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (recipient_id) REFERENCES users(id)
);
