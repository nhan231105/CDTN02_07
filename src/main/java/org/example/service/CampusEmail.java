package org.example.service;

import org.example.common.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class CampusEmail {
    private final RestClient client;
    private final String key;
    private final Map<String, String> sender;

    @org.springframework.beans.factory.annotation.Autowired
    public CampusEmail(@Value("${BREVO_API_KEY:}") String key,
                       @Value("${CAMPUSWORK_EMAIL_FROM:}") String from) {
        this(key, from, clientBuilder());
    }

    CampusEmail(String key, String from, RestClient.Builder builder) {
        this.key = key.strip();
        sender = parseSender(from);
        client = builder.baseUrl("https://api.brevo.com").build();
    }

    private static RestClient.Builder clientBuilder() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));
        return RestClient.builder().requestFactory(factory);
    }

    public void ensureConfigured() {
        if (key.isBlank() || sender.isEmpty()) {
            log.warn("CampusWork email configuration invalid apiKeyPresent={} senderValid={}",
                    !key.isBlank(), !sender.isEmpty());
            throw new DeliveryException();
        }
    }

    public void send(String email, String subject, String code) {
        sendMessage(email, subject, "Mã CampusWork của bạn là " + code + ". Mã hết hạn sau 5 phút.",
                UUID.randomUUID());
    }

    public void sendMessage(String email, String subject, String body, UUID idempotencyKey) {
        ensureConfigured();
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
                log.warn("CampusWork email provider returned HTTP {}", response.getStatusCode().value());
            } else {
                log.warn("CampusWork email provider request failed");
            }
            throw new DeliveryException();
        }
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
