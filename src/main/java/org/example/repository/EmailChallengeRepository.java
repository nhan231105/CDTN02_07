package org.example.repository;

import org.example.entity.EmailChallenge;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface EmailChallengeRepository extends JpaRepository<EmailChallenge, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from EmailChallenge c where c.id = :id")
    Optional<EmailChallenge> findLocked(String id);
}
