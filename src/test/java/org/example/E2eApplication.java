package org.example;

import org.example.service.CampusEmail;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class E2eApplication {
    public static void main(String[] args) throws IOException {
        String outbox = System.getenv("CAMPUSWORK_E2E_OUTBOX");
        if (outbox == null || outbox.isBlank()) {
            throw new IllegalStateException("CAMPUSWORK_E2E_OUTBOX is required");
        }
        Files.createDirectories(Path.of(outbox));
        new SpringApplicationBuilder(Main.class, MailFixture.class)
                .profiles("dev-h2", "e2e-mail").run(
                        "--server.address=127.0.0.1",
                        "--spring.datasource.url=jdbc:h2:mem:campuswork_e2e",
                        "--spring.datasource.username=sa",
                        "--spring.datasource.driver-class-name=org.h2.Driver",
                        "--spring.jpa.hibernate.ddl-auto=create-drop",
                        "--logging.level.org.hibernate.SQL=WARN",
                        "--file.upload.directory=target/e2e-uploads");
    }

    @TestConfiguration
    @Profile("e2e-mail")
    static class MailFixture {
        @Bean
        @Primary
        CampusEmail localOutbox() {
            Path outbox = Path.of(System.getenv("CAMPUSWORK_E2E_OUTBOX"));
            return new CampusEmail("", "") {
                @Override
                public void ensureConfigured() {}

                @Override
                public void send(String email, String subject, String code) {
                    if (!email.matches("[a-z0-9.]+@test\\.local")) {
                        throw new IllegalArgumentException("Expected a synthetic test address");
                    }
                    try {
                        Files.writeString(outbox.resolve(email), code);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            };
        }
    }
}
