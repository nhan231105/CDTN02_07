package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.DomainException;
import org.example.entity.*;
import org.example.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEmailService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_SECONDS = 300;
    private static final int RESEND_SECONDS = 60;
    private static final int WINDOW_SECONDS = 3600;
    private static final int MAX_SENDS = 4;
    private static final int MAX_ATTEMPTS = 5;

    private final EmailChallengeRepository challenges;
    private final UserRepository users;
    private final UserService userService;
    private final BCryptPasswordEncoder passwords;
    private final CampusEmail mail;

    @Transactional(noRollbackFor = CampusEmail.DeliveryException.class)
    public void requestRegistration(RegisterRequest request) {
        validateRegistration(request);
        mail.ensureConfigured();
        String email = normalize(request.getEmail());
        boolean emailTaken = users.existsByEmail(email);
        boolean phoneTaken = !emailTaken && users.existsByPhone(request.getPhone());
        issue("register", email, emailTaken || phoneTaken ? null : "");
        if (emailTaken || phoneTaken) {
            log.info("Registration OTP suppressed reason={}", emailTaken ? "EMAIL_IN_USE" : "PHONE_IN_USE");
        } else {
            log.info("Registration OTP accepted by email provider");
        }
    }

    @Transactional(noRollbackFor = InvalidCode.class)
    public RegisterResponse verifyRegistration(RegisterRequest request, String code) {
        validateRegistration(request);
        verify("register", normalize(request.getEmail()), code, "");
        return userService.register(request);
    }

    @Transactional(noRollbackFor = CampusEmail.DeliveryException.class)
    public void requestReset(String address) {
        mail.ensureConfigured();
        String email = normalize(address);
        User user = users.findByEmail(email).orElse(null);
        issue("reset", email, version(user));
    }

    @Transactional(noRollbackFor = InvalidCode.class)
    public void reset(String address, String code, String password) {
        validatePassword(password);
        String email = normalize(address);
        User user = users.findByEmail(email).orElse(null);
        if (user != null) {
            user = users.findLockedById(user.getId()).orElse(null);
        }
        String version = user != null && email.equalsIgnoreCase(user.getEmail()) ? version(user) : null;
        verify("reset", email, code, version);
        user.setPasswordHash(passwords.encode(password));
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    private void issue(String purpose, String email, String accountVersion) {
        String id = purpose + ":" + email;
        EmailChallenge challenge = challenges.findLocked(id).orElseGet(() -> {
            EmailChallenge created = new EmailChallenge();
            created.setId(id);
            return challenges.saveAndFlush(created);
        });
        Instant now = Instant.now();
        if (challenge.getSentAt() != null && now.isBefore(challenge.getSentAt().plusSeconds(RESEND_SECONDS))) {
            throw new DomainException(429, "OTP_COOLDOWN", "Vui lòng đợi 60 giây trước khi yêu cầu mã mới.");
        }
        if (challenge.getWindowStartedAt() == null ||
                !now.isBefore(challenge.getWindowStartedAt().plusSeconds(WINDOW_SECONDS))) {
            challenge.setWindowStartedAt(now);
            challenge.setSends(0);
        }
        if (challenge.getSends() >= MAX_SENDS) {
            throw new DomainException(429, "OTP_LIMIT", "Đã đạt giới hạn gửi mã. Vui lòng thử lại sau.");
        }
        String code;
        do {
            code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        } while (challenge.getCodeHash() != null && passwords.matches(code, challenge.getCodeHash()));
        challenge.setCodeHash(passwords.encode(code));
        challenge.setAccountVersion(accountVersion);
        challenge.setSentAt(now);
        challenge.setAttempts(0);
        challenge.setSends(challenge.getSends() + 1);
        if (accountVersion != null) {
            boolean registration = purpose.equals("register");
            try {
                mail.send(email, registration ? "Xác minh email CampusWork" : "Đặt lại mật khẩu CampusWork", code);
            } catch (CampusEmail.DeliveryException failure) {
                challenge.setCodeHash(null);
                throw failure;
            }
        }
    }

    private void verify(String purpose, String email, String code, String accountVersion) {
        EmailChallenge challenge = challenges.findLocked(purpose + ":" + email).orElseThrow(InvalidCode::new);
        if (challenge.getCodeHash() == null || challenge.getAttempts() >= MAX_ATTEMPTS ||
                !Instant.now().isBefore(challenge.getSentAt().plusSeconds(OTP_SECONDS))) {
            throw new InvalidCode();
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!passwords.matches(code, challenge.getCodeHash()) ||
                accountVersion == null || !accountVersion.equals(challenge.getAccountVersion())) {
            throw new InvalidCode();
        }
        challenge.setCodeHash(null);
    }

    private static String version(User user) {
        return user != null && user.getStatus() == UserStatus.ACTIVE
                ? user.getId() + ":" + user.getTokenVersion() : null;
    }

    private static void validateRegistration(RegisterRequest request) {
        if (request.getFullName().trim().length() < 3) {
            throw new DomainException(400, "INVALID_NAME", "Tên cần ít nhất 3 ký tự.");
        }
        if (!"STUDENT".equalsIgnoreCase(request.getRole()) && !"EMPLOYER".equalsIgnoreCase(request.getRole())) {
            throw new DomainException(400, "INVALID_ROLE", "Chỉ được đăng ký Sinh viên hoặc Nhà tuyển dụng.");
        }
        validatePassword(request.getPassword());
    }

    private static void validatePassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new DomainException(400, "INVALID_PASSWORD", "Mật khẩu quá dài. Vui lòng dùng ít ký tự hơn.");
        }
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static class InvalidCode extends DomainException {
        InvalidCode() {
            super(400, "INVALID_OTP", "Mã xác minh không hợp lệ, đã hết hạn hoặc đã hết lượt thử.");
        }
    }
}
