package campuswork.seed;

import org.example.entity.*;
import org.example.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeedCommandTest {
    @TempDir Path directory;
    private final LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Test
    void insertsFortyJobsAndKeepsCredentialsAndExistingRecordsOnRerun() throws Exception {
        var catalog = SeedCatalog.load(today);
        var uploads = directory.resolve("uploads");
        var exports = directory.resolve("exports");
        try (var context = SeedCommand.start(settings(uploads))) {
            var runner = context.getBean(SeedRunner.class);
            var users = context.getBean(UserRepository.class);
            var profiles = context.getBean(EmployerProfileRepository.class);
            var jobs = context.getBean(JobRepository.class);
            var files = context.getBean(StoredFileRepository.class);
            var encoder = context.getBean(BCryptPasswordEncoder.class);
            var existing = users.saveAndFlush(existingUser("0000000099"));

            var first = runner.apply(catalog, today, exports);
            assertThat(first.companies()).isEqualTo(15);
            assertThat(first.jobs()).isEqualTo(40);
            assertThat(first.skipped()).isZero();
            assertThat(users.count()).isEqualTo(16);
            assertThat(profiles.count()).isEqualTo(15);
            assertThat(files.count()).isEqualTo(15);

            var lines = Files.readAllLines(first.credentials());
            assertThat(lines).hasSize(16);
            assertThat(lines.getFirst()).isEqualTo("company_key,email,phone,password");
            var passwords = new HashSet<String>();
            for (String row : lines.subList(1, lines.size())) {
                var fields = row.split(",");
                var user = users.findByEmail(fields[1]).orElseThrow();
                assertThat(user.getRole()).isEqualTo(UserRole.EMPLOYER);
                assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
                assertThat(user.getPhone()).isEqualTo(fields[2]);
                assertThat(encoder.matches(fields[3], user.getPasswordHash())).isTrue();
                assertThat(passwords.add(fields[3])).isTrue();
            }
            for (var company : catalog) {
                var owner = users.findByEmail(company.email()).orElseThrow();
                var profile = profiles.findById(owner.getId()).orElseThrow();
                assertThat(profile.getOrgName()).startsWith("[DEMO] ");
                assertThat(profile.getLogoRef()).isEqualTo(company.logoRef().toString());
                var file = files.findById(profile.getLogoRef()).orElseThrow();
                assertThat(file.getContentType()).isEqualTo("image/png");
                var storage = context.getBean(org.example.service.FileStorageService.class);
                assertThat(storage.read(file)).isEqualTo(company.png());
                assertThat(uploads.resolve(file.getStorageName())).doesNotExist();
                assertThat(jobs.findByEmployerIdAndRemovedByAdminFalse(owner.getId()))
                        .hasSize(company.jobs().size()).allSatisfy(job -> {
                            assertThat(job.getTitle()).startsWith("[DEMO] ");
                            assertThat(job.getJobType()).isEqualTo(JobType.PART_TIME);
                            assertThat(job.getPayType()).isEqualTo(PayType.HOURLY);
                            assertThat(job.getDeadline()).isBetween(today.plusDays(30), today.plusDays(60));
                        });
            }

            var receipt = Files.readAllLines(first.receipt());
            assertThat(receipt).hasSize(16);
            assertThat(receipt.getFirst()).isEqualTo("company_key,user_id,logo_ref,job_ids");
            assertThat(receipt.subList(1, receipt.size()).stream()
                    .mapToInt(row -> row.split(",")[3].split(";").length).sum()).isEqualTo(40);
            assertThat(runner.preview(catalog, uploads)).isZero();

            var edited = users.findByEmail(catalog.getFirst().email()).orElseThrow();
            var originalHash = edited.getPasswordHash();
            edited.setEmail("changed@example.invalid");
            edited.setPhone("0000000088");
            edited.setFullName("Tên đã sửa");
            users.saveAndFlush(edited);
            assertThat(runner.apply(catalog, today.plusDays(1), exports))
                    .isEqualTo(new SeedRunner.Result(0, 0, 15, null, null));
            var unchanged = users.findById(edited.getId()).orElseThrow();
            assertThat(unchanged.getPasswordHash()).isEqualTo(originalHash);
            assertThat(unchanged.getFullName()).isEqualTo("Tên đã sửa");
            assertThat(users.findById(existing.getId()).orElseThrow().getEmail()).isEqualTo(existing.getEmail());
            assertThat(jobs.count()).isEqualTo(40);
            try (var exportsFound = Files.list(exports)) {
                assertThat(exportsFound.toList()).containsExactlyInAnyOrder(first.credentials(), first.receipt());
            }
        }
    }

    @Test
    void preservesConflictsAndRollsBackImageWhenProfileIsMissing() throws Exception {
        var catalog = SeedCatalog.load(today);
        var uploads = Files.createDirectory(directory.resolve("uploads"));
        var exports = directory.resolve("exports");
        var conflict = uploads.resolve(catalog.get(2).logoRef() + ".png");
        Files.writeString(conflict, "preserve this file");
        try (var context = SeedCommand.start(settings(uploads))) {
            var users = context.getBean(UserRepository.class);
            var profiles = context.getBean(EmployerProfileRepository.class);
            var jobs = context.getBean(JobRepository.class);
            var files = context.getBean(StoredFileRepository.class);
            var runner = context.getBean(SeedRunner.class);
            var existing = users.saveAndFlush(existingUser(catalog.getFirst().phone()));

            assertThat(runner.preview(catalog, uploads)).isEqualTo(2);
            assertThatThrownBy(() -> runner.apply(catalog, today, exports, uploads, true))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("conflicts");
            assertThat(exports).doesNotExist();
            var storage = context.getBean(org.example.service.FileStorageService.class);
            var invalidRef = UUID.randomUUID();
            var logo = new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", catalog.getFirst().png());
            assertThatThrownBy(() -> storage.uploadLogo(existing.getId(), logo, invalidRef))
                    .isInstanceOf(RuntimeException.class).hasMessageContaining("hồ sơ");
            assertThat(storage.hasStoredImage(invalidRef.toString())).isFalse();
            assertThat(users.count()).isOne();
            assertThat(profiles.count()).isZero();
            assertThat(jobs.count()).isZero();
            assertThat(files.count()).isZero();
            assertThat(Files.readString(conflict)).isEqualTo("preserve this file");
            assertThat(uploads.resolve(catalog.get(1).logoRef() + ".png")).doesNotExist();
            assertThat(exports).doesNotExist();

            Files.delete(conflict);
            var recovered = runner.apply(catalog, today, exports);
            assertThat(recovered.companies()).isEqualTo(14);
            assertThat(recovered.jobs()).isEqualTo(37);
            assertThat(profiles.findById(existing.getId())).isEmpty();
            assertThat(users.findById(existing.getId()).orElseThrow().getPasswordHash()).isEqualTo(existing.getPasswordHash());
        }
    }

    @Test
    void validatesDryRunAndRejectsIncorrectTargets() throws Exception {
        SeedCommand.run(new String[]{"--dry-run"}, Map.of());
        assertThatThrownBy(() -> SeedCommand.run(new String[]{"--apply", "--dry-run"}, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SeedCommand.run(new String[]{"--apply", "--mysql-test"},
                Map.of("CAMPUSWORK_SEED_JDBC_URL", "jdbc:mysql://127.0.0.1/main")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("staging");
        assertThatThrownBy(() -> SeedCommand.run(new String[]{"--dry-run", "--check-render-disk"},
                Map.of("UPLOAD_DIRECTORY", "uploads")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("UPLOAD_DIRECTORY");
    }

    @Test
    void productionNeedsMatchingTargetTlsAndSeparateApplyConfirmation() {
        var env = new java.util.HashMap<>(Map.of(
                "RENDER", "true", "DB_URL", "jdbc:mysql://mysql.example:53306/railway",
                "CAMPUSWORK_SEED_JDBC_URL", "jdbc:mysql://mysql.example:53306/railway?sslMode=VERIFY_IDENTITY",
                "CAMPUSWORK_SEED_DB_USERNAME", "fixture", "CAMPUSWORK_SEED_DB_PASSWORD", "fixture"));
        assertThat(SeedMySql.target(env, true, false, false).username()).isEqualTo("fixture");
        assertThatThrownBy(() -> SeedMySql.target(env, true, true, true)).hasMessageContaining("approval");
        assertThat(SeedMySql.target(env, true, true, false).username()).isEqualTo("fixture");
        env.put("CAMPUSWORK_SEED_PRODUCTION_CONFIRM", "approved-campuswork-demo-v1");
        assertThat(SeedMySql.target(env, true, true, true).username()).isEqualTo("fixture");
        env.put("DB_URL", "jdbc:mysql://mysql.example:53306/other");
        assertThatThrownBy(() -> SeedMySql.target(env, true, true, true)).hasMessageContaining("must match");
        env.put("DB_URL", "jdbc:mysql://mysql.example:53306/railway");
        env.put("CAMPUSWORK_SEED_JDBC_URL", "jdbc:mysql://mysql.example:53306/railway?sslMode=REQUIRED");
        assertThatThrownBy(() -> SeedMySql.target(env, true, true, true)).hasMessageContaining("VERIFY_IDENTITY");
    }

    private Map<String, Object> settings(Path uploads) {
        return SeedCommand.databaseSettings("jdbc:h2:mem:" + UUID.randomUUID(), "sa", "", uploads, "create-drop");
    }

    private User existingUser(String phone) {
        var user = new User();
        user.setFullName("Tài khoản có sẵn");
        user.setEmail("keep@example.invalid");
        user.setPhone(phone);
        user.setPasswordHash("fixture-hash");
        user.setRole(UserRole.STUDENT);
        return user;
    }
}
