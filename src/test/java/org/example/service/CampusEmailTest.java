package org.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CampusEmailTest {
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final CampusEmail mail = new CampusEmail("test-only", "CampusWork <sender@example.test>", builder);

    @Test
    void sendsBrevoTransactionalPayload() {
        UUID key = UUID.randomUUID();
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "test-only"))
                .andExpect(jsonPath("$.sender.name").value("CampusWork"))
                .andExpect(jsonPath("$.sender.email").value("sender@example.test"))
                .andExpect(jsonPath("$.to[0].email").value("recipient@example.test"))
                .andExpect(jsonPath("$.textContent").value("Nội dung thử"))
                .andExpect(jsonPath("$.headers.IdempotencyKey").value(key.toString()))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON).body("{\"messageId\":\"test-id\"}"));
        mail.sendMessage("recipient@example.test", "Thông báo", "Nội dung thử", key);
        server.verify();
    }

    @Test
    void rejectsProviderErrors() {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThrows(CampusEmail.DeliveryException.class,
                () -> mail.send("recipient@example.test", "Xác minh", "123456"));
        server.verify();
    }

    @Test
    void rejectsAcknowledgmentsWithoutMessageId() {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON).body("{}"));
        assertThrows(CampusEmail.DeliveryException.class,
                () -> mail.send("recipient@example.test", "Xác minh", "123456"));
        server.verify();
    }
}
