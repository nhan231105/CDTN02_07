package org.example.service;

import jakarta.mail.MessagingException;
import org.example.common.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Transactional email gateway used for OTP codes and application notifications.
 *
 * <p>Provider selection ({@code CAMPUSWORK_EMAIL_PROVIDER}):
 * <ul>
 *   <li>{@code auto} (default): Brevo when {@code BREVO_API_KEY} is set, otherwise SMTP when
 *       {@code SMTP_HOST} is set, otherwise email is unavailable (HTTP 503 EMAIL_UNAVAILABLE).</li>
 *   <li>{@code brevo}: Brevo HTTP API ({@code BREVO_API_KEY}).</li>
 *   <li>{@code smtp}: any SMTP server ({@code SMTP_HOST}, {@code SMTP_PORT}, {@code SMTP_USERNAME},
 *       {@code SMTP_PASSWORD}), e.g. Gmail with an App Password.</li>
 *   <li>{@code log}: local development only. Nothing is sent; the message (including the OTP) is
 *       written to the backend log. Refused when {@code campuswork.email.log-provider-allowed=false}
 *       (set by the {@code render} profile).</li>
 * </ul>
 * Secrets (API key, SMTP password) are never written to logs.
 */
@Component
@Slf4j
public class CampusEmail {
    enum Provider { BREVO, SMTP, LOG, NONE }

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final RestClient client;
    private final String key;
    private final Map<String, String> sender;
    private final JavaMailSender smtp;
    private final Provider provider;

    @Autowired
    public CampusEmail(@Value("${BREVO_API_KEY:}") String key,
                       @Value("${CAMPUSWORK_EMAIL_FROM:}") String from,
                       @Value("${CAMPUSWORK_EMAIL_PROVIDER:auto}") String provider,
                       @Value("${SMTP_HOST:}") String smtpHost,
                       @Value("${SMTP_PORT:587}") int smtpPort,
                       @Value("${SMTP_USERNAME:}") String smtpUsername,
                       @Value("${SMTP_PASSWORD:}") String smtpPassword,
                       @Value("${campuswork.email.log-provider-allowed:true}") boolean logProviderAllowed) {
        this.key = key.strip();
        this.smtp = smtpSender(smtpHost, smtpPort, smtpUsername, smtpPassword);
        String effectiveFrom = from.isBlank() && smtpUsername.contains("@") ? smtpUsername : from;
        this.sender = parseSender(effectiveFrom);
        this.client = clientBuilder().baseUrl("https://api.brevo.com").build();
        this.provider = resolve(provider, !this.key.isBlank(), smtp != null, logProviderAllowed);
        logStartupState();
    }

    /** Brevo-only constructor (kept for test fixtures and subclasses). */
    public CampusEmail(String key, String from) {
        this(key, from, clientBuilder());
    }

    CampusEmail(String key, String from, RestClient.Builder builder) {
        this.key = key.strip();
        this.sender = parseSender(from);
        this.smtp = null;
        this.client = builder.baseUrl("https://api.brevo.com").build();
        this.provider = this.key.isBlank() ? Provider.NONE : Provider.BREVO;
    }

    private static RestClient.Builder clientBuilder() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS)).build());
        factory.setReadTimeout(Duration.ofMillis(READ_TIMEOUT_MS));
        return RestClient.builder().requestFactory(factory);
    }

    private static JavaMailSender smtpSender(String host, int port, String username, String password) {
        if (host == null || host.isBlank()) return null;
        var mailer = new JavaMailSenderImpl();
        mailer.setHost(host.strip());
        mailer.setPort(port);
        mailer.setDefaultEncoding("UTF-8");
        boolean auth = username != null && !username.isBlank();
        if (auth) {
            mailer.setUsername(username.strip());
            mailer.setPassword(password);
        }
        Properties props = mailer.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(auth));
        if (port == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.connectiontimeout", String.valueOf(CONNECT_TIMEOUT_MS));
        props.put("mail.smtp.timeout", String.valueOf(READ_TIMEOUT_MS));
        props.put("mail.smtp.writetimeout", String.valueOf(READ_TIMEOUT_MS));
        return mailer;
    }

    private static Provider resolve(String requested, boolean brevoKey, boolean smtpHost, boolean logAllowed) {
        String value = requested == null ? "auto" : requested.strip().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "", "auto" -> brevoKey ? Provider.BREVO : smtpHost ? Provider.SMTP : Provider.NONE;
            case "brevo" -> Provider.BREVO;
            case "smtp" -> Provider.SMTP;
            case "log" -> {
                if (!logAllowed) {
                    throw new IllegalStateException(
                            "CAMPUSWORK_EMAIL_PROVIDER=log is for local development only and is disabled in this profile");
                }
                yield Provider.LOG;
            }
            default -> throw new IllegalStateException(
                    "Unknown CAMPUSWORK_EMAIL_PROVIDER '" + requested + "'. Use auto, brevo, smtp or log");
        };
    }

    private void logStartupState() {
        switch (provider) {
            case NONE -> log.warn("CampusWork email is NOT configured: OTP for registration/password reset/password "
                    + "change will fail with 503 EMAIL_UNAVAILABLE. Set BREVO_API_KEY or SMTP_HOST/SMTP_USERNAME/"
                    + "SMTP_PASSWORD, plus CAMPUSWORK_EMAIL_FROM (see .env.example).");
            case LOG -> log.warn("CampusWork email provider=LOG: emails are NOT delivered, OTP codes are printed "
                    + "to this log. Never use this outside local development.");
            default -> {
                if (problem() != null) {
                    log.warn("CampusWork email provider={} is incomplete: {}", provider, problem());
                } else {
                    log.info("CampusWork email provider={} sender={}", provider, sender.get("email"));
                }
            }
        }
    }

    /** Returns a human-readable description of the missing configuration, or null when ready. */
    private String problem() {
        return switch (provider) {
            case NONE -> "no email provider configured (BREVO_API_KEY or SMTP_HOST)";
            case LOG -> null;
            case BREVO -> key.isBlank() ? "BREVO_API_KEY is empty"
                    : sender.isEmpty() ? "CAMPUSWORK_EMAIL_FROM is missing or invalid (expected 'Name <email>' or 'email')"
                    : null;
            case SMTP -> smtp == null ? "SMTP_HOST is empty"
                    : sender.isEmpty() ? "CAMPUSWORK_EMAIL_FROM (or an email-shaped SMTP_USERNAME) is missing or invalid"
                    : null;
        };
    }

    public void ensureConfigured() {
        String problem = problem();
        if (problem != null) {
            log.warn("CampusWork email unavailable provider={} reason={}", provider, problem);
            throw new DeliveryException();
        }
    }

    public void send(String email, String subject, String code) {
        sendMessage(email, subject,
                "Mã xác minh CampusWork của bạn là: " + code + "\n\n"
                        + "Mã có hiệu lực trong 5 phút và chỉ dùng được một lần.\n"
                        + "Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email. "
                        + "Tuyệt đối không chia sẻ mã cho bất kỳ ai.",
                UUID.randomUUID());
    }

    public void sendMessage(String email, String subject, String body, UUID idempotencyKey) {
        ensureConfigured();
        switch (provider) {
            case BREVO -> sendBrevo(email, subject, body, idempotencyKey);
            case SMTP -> sendSmtp(email, subject, body);
            case LOG -> log.warn("[DEV EMAIL - NOT SENT] to={} subject=\"{}\"\n{}", email, subject, body);
            default -> throw new DeliveryException();
        }
    }

    private void sendBrevo(String email, String subject, String body, UUID idempotencyKey) {
        try {
            var response = client.post().uri("/v3/smtp/email").header("api-key", key)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("sender", sender, "to", List.of(Map.of("email", email)),
                            "subject", subject, "textContent", body,
                            "headers", Map.of("IdempotencyKey", idempotencyKey.toString())))
                    .retrieve().toEntity(AcceptedEmail.class);
            var accepted = response.getBody();
            if (response.getStatusCode().value() != 201 || accepted == null
                    || accepted.messageId() == null || accepted.messageId().isBlank()) {
                log.warn("CampusWork email provider did not acknowledge delivery request HTTP {}",
                        response.getStatusCode().value());
                throw new DeliveryException();
            }
        } catch (RestClientException ex) {
            if (ex instanceof RestClientResponseException response) {
                int status = response.getStatusCode().value();
                log.warn("CampusWork email provider returned HTTP {} body={} hint={}", status,
                        abbreviate(response.getResponseBodyAsString()), brevoHint(status));
            } else {
                log.warn("CampusWork email provider request failed: {} {}",
                        ex.getClass().getSimpleName(), abbreviate(ex.getMessage()));
            }
            throw new DeliveryException();
        }
    }

    private void sendSmtp(String email, String subject, String body) {
        try {
            var message = smtp.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(sender.get("email"), sender.get("name"));
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(body, false);
            smtp.send(message);
        } catch (MailException | MessagingException | UnsupportedEncodingException ex) {
            // Spring/Jakarta mail exception messages never include the SMTP password.
            log.warn("CampusWork SMTP delivery failed: {} {}", ex.getClass().getSimpleName(),
                    abbreviate(ex.getMessage()));
            throw new DeliveryException();
        }
    }

    private static String brevoHint(int status) {
        return switch (status) {
            case 400 -> "check that CAMPUSWORK_EMAIL_FROM is a verified sender in Brevo (Senders, Domains & IPs)";
            case 401 -> "invalid BREVO_API_KEY, or the server IP is not in Brevo Security > Authorized IPs";
            case 403 -> "Brevo account cannot send transactional email yet (activation/suspension)";
            case 429 -> "Brevo rate limit or daily quota reached";
            default -> "see Brevo transactional logs";
        };
    }

    private static String abbreviate(String text) {
        if (text == null) return "";
        String flat = text.replaceAll("[\\r\\n]+", " ").strip();
        return flat.length() > 300 ? flat.substring(0, 300) + "..." : flat;
    }

    private static Map<String, String> parseSender(String from) {
        var match = java.util.regex.Pattern.compile("^(?:([^<>]+)\\s*<([^<>]+)>|([^<>]+))$")
                .matcher(from.strip());
        if (!match.matches()) return Map.of();
        String email = (match.group(2) == null ? match.group(3) : match.group(2)).strip();
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) return Map.of();
        String name = match.group(1) == null ? "CampusWork" : match.group(1).strip();
        return Map.of("name", name, "email", email);
    }

    private record AcceptedEmail(String messageId) {}

    public static final class DeliveryException extends DomainException {
        public DeliveryException() {
            super(503, "EMAIL_UNAVAILABLE", "Chưa thể gửi email. Vui lòng thử lại sau.");
        }
    }
}
