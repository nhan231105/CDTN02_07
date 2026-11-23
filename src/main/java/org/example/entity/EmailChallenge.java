package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "email_challenges")
@Getter
@Setter
public class EmailChallenge {
    @Id
    @Column(length = 170)
    private String id;

    @Column(length = 60)
    private String codeHash;

    private Instant windowStartedAt;
    private Instant sentAt;
    private int sends;
    private int attempts;
    private String accountVersion;
}
