package org.example.service;

import org.example.common.DomainException;
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

@Component
public class CampusEmail {
    private final RestClient client;
    private final String key;
    private final String from;

    public CampusEmail(@Value("${RESEND_API_KEY:}") String key,
                       @Value("${CAMPUSWORK_EMAIL_FROM:}") String from) {
        this.key = key;
        this.from = from;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));
        client = RestClient.builder().requestFactory(factory)
                .baseUrl("https://api.resend.com").build();
    }

    public void ensureConfigured() {
        if (key.isBlank() || from.isBlank()) throw unavailable();
    }

    public void send(String email, String subject, String code) {
        ensureConfigured();
        try {
            client.post().uri("/emails").headers(headers -> headers.setBearerAuth(key))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("from", from, "to", List.of(email), "subject", subject,
                            "text", "Mã CampusWork của bạn là " + code + ". Mã hết hạn sau 5 phút."))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            var log = org.slf4j.LoggerFactory.getLogger(CampusEmail.class);
            if (ex instanceof RestClientResponseException response) {
                log.warn("CampusWork email provider returned HTTP {}", response.getStatusCode().value());
            } else {
                log.warn("CampusWork email provider request failed");
            }
            throw unavailable();
        }
    }

    private DomainException unavailable() {
        return new DomainException(503, "EMAIL_UNAVAILABLE", "Chưa thể gửi email. Vui lòng thử lại sau.");
    }
}
