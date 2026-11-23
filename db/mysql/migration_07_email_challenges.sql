CREATE TABLE IF NOT EXISTS email_challenges (
    id VARCHAR(170) NOT NULL PRIMARY KEY,
    code_hash VARCHAR(60),
    window_started_at DATETIME(6),
    sent_at DATETIME(6),
    sends INT NOT NULL,
    attempts INT NOT NULL,
    account_version VARCHAR(255)
);
