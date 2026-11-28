package campuswork.seed;

import org.example.config.SecurityConfig;
import org.example.common.DomainException;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.FileStorageService;
import org.example.service.JobServiceImpl;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

public final class SeedCommand {
    private static final Path LOCAL_ROOT = Path.of("secrets", "campuswork-seed-test").toAbsolutePath();
    private static final Path RENDER_DISK = Path.of("/var/data");


    private SeedCommand() {}

    public static void run(String[] arguments) {
        try {
            run(arguments, System.getenv());
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalStateException("CampusWork seed: " + e.getMessage());
        } catch (DomainException e) {
            throw new IllegalStateException("CampusWork seed: " + e.getMessage());
        } catch (Exception e) {
            // Avoid leaking environment-derived connection details in the CLI error.
            throw new IllegalStateException("CampusWork seed failed (" + e.getClass().getSimpleName() + ").");
        }
    }

    static void run(String[] arguments, Map<String, String> environment) throws Exception {
        var flags = new HashSet<>(List.of(arguments));
        if (flags.size() != arguments.length
                || !Set.of("--apply", "--dry-run", "--mysql-test", "--mysql-production", "--check-render-disk").containsAll(flags)
                || flags.contains("--dry-run") && flags.contains("--apply")) {
            throw new IllegalArgumentException("Use --campuswork-seed [--dry-run | --apply] [--mysql-test | --mysql-production] [--check-render-disk]");
        }
        boolean render = flags.contains("--check-render-disk");
        boolean production = flags.contains("--mysql-production");
        boolean mysqlTest = flags.contains("--mysql-test");
        if (production && mysqlTest) {
            throw new IllegalArgumentException("Choose one MySQL target.");
        }
        boolean mysql = production || mysqlTest;
        boolean apply = flags.contains("--apply");
        if (apply && render && !mysql) {
            throw new IllegalArgumentException("Render apply requires an explicit MySQL target.");
        }
        var target = mysql ? SeedMySql.target(environment, production, render, apply) : null;
        if (apply && "true".equalsIgnoreCase(environment.get("RENDER"))) {
            throw new IllegalArgumentException("Run apply from the operator workstation to keep the private CSV local.");
        }

        var today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        var logoDirectory = Path.of(environment.getOrDefault("CAMPUSWORK_SEED_LOGO_DIRECTORY", "db/seed-assets/logos"));
        var companies = SeedCatalog.load(today, logoDirectory);
        System.out.printf("Validated %d companies, %d jobs and %d distinct PNG logos.%n",
                companies.size(), companies.stream().mapToInt(c -> c.jobs().size()).sum(), companies.size());

        Path uploads = LOCAL_ROOT.resolve("uploads");
        Path exports = LOCAL_ROOT.resolve("credentials");
        if (render) {
            uploads = renderUploads(environment.get("UPLOAD_DIRECTORY"));

            System.out.println("Render upload path is on the writable /var/data mount.");
        }
        if (!apply && !mysql) {
            System.out.println("Offline dry-run complete. Database, uploads and credentials untouched.");
            return;
        }

        String jdbc = "jdbc:h2:file:" + LOCAL_ROOT.resolve("campuswork").toString().replace('\\', '/');
        String username = "sa";
        String password = "";
        if (target != null) {
            jdbc = target.url();
            username = target.username();
            password = target.password();
        }
        if (apply && !render) {
            Files.createDirectories(LOCAL_ROOT);
        }

        var settings = databaseSettings(jdbc, username, password, uploads, mysql ? "validate" : "update");
        try (var context = start(settings)) {
            if (mysql) {
                SeedMySql.checkSchema(context.getBean(JdbcTemplate.class), context.getBean(PlatformTransactionManager.class));
                System.out.println("MySQL schema and seed constraints validated.");
            }
            var runner = context.getBean(SeedRunner.class);
            if (!apply) {
                if (runner.preview(companies, uploads) > 0) {
                    throw new IllegalArgumentException("Dry-run found conflicts. No database or file changes were made.");
                }
                System.out.println("MySQL dry-run complete. No database or file changes were made.");
                return;
            }
            var result = runner.apply(companies, today, exports, uploads, mysql);
            System.out.printf("Created %d companies and %d jobs. Skipped %d companies.%n",
                    result.companies(), result.jobs(), result.skipped());
            if (result.credentials() != null) {
                if (Files.exists(result.credentials())) System.out.println("Private credentials file: " + result.credentials());
                if (Files.exists(result.receipt())) System.out.println("Rollback receipt: " + result.receipt());
                if (!Files.exists(result.credentials()) || !Files.exists(result.receipt())) {
                    throw new IllegalArgumentException("Database committed. Recover the pending files reported above before deploying.");
                }
            }
        }
    }

    static Map<String, Object> databaseSettings(String jdbc, String username, String password, Path uploads, String ddl) {
        return Map.of(
                "spring.datasource.url", jdbc,
                "spring.datasource.username", username,
                "spring.datasource.password", password,
                "spring.datasource.driver-class-name", jdbc.startsWith("jdbc:h2:") ? "org.h2.Driver" : "com.mysql.cj.jdbc.Driver",
                "spring.jpa.hibernate.ddl-auto", ddl,
                "spring.jpa.show-sql", false,
                "spring.sql.init.mode", "never",
                "file.upload.directory", uploads.toString());
    }

    static org.springframework.context.ConfigurableApplicationContext start(Map<String, Object> settings) {
        var environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return new SpringApplicationBuilder(SeedConfiguration.class).web(WebApplicationType.NONE)
                .environment(environment)
                .registerShutdownHook(false).logStartupInfo(false).bannerMode(org.springframework.boot.Banner.Mode.OFF)
                .initializers(context -> context.getEnvironment().getPropertySources()
                        .addFirst(new MapPropertySource("explicitSeedTarget", settings)))
                .run("--logging.level.root=OFF", "--spring.config.name=campuswork-seed-command",
                     "--spring.config.location=optional:classpath:/campuswork-seed-command.properties");
    }

    private static Path renderUploads(String configured) throws IOException {
        if (configured == null || !Path.of(configured).isAbsolute()) {
            throw new IllegalArgumentException("Set UPLOAD_DIRECTORY to an absolute subdirectory of /var/data.");
        }
        Path uploads = Path.of(configured).normalize();
        if (!uploads.startsWith(RENDER_DISK) || uploads.equals(RENDER_DISK)
                || uploads.startsWith(RENDER_DISK.resolve("secrets"))) {
            throw new IllegalArgumentException("Render uploads must be under /var/data and outside its secrets directory.");
        }
        boolean mounted = Files.readAllLines(Path.of("/proc/self/mountinfo")).stream()
                .map(line -> line.split(" "))
                .anyMatch(fields -> fields.length > 5 && fields[4].equals(RENDER_DISK.toString())
                        && List.of(fields[5].split(",")).contains("rw"));
        if (!mounted) throw new IllegalArgumentException("/var/data is not a writable mounted disk.");
        verifyDiskDirectory(uploads);
        return uploads;
    }

    private static void verifyDiskDirectory(Path path) throws IOException {
        Path existing = path;
        while (!Files.exists(existing, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
            existing = existing.getParent();
        }
        Path realDirectory = existing.toRealPath();
        if (!realDirectory.equals(existing) || !realDirectory.startsWith(RENDER_DISK)
                || !Files.isDirectory(realDirectory) || !Files.isWritable(realDirectory)
                || !Files.getFileStore(realDirectory).equals(Files.getFileStore(RENDER_DISK))) {
            throw new IllegalArgumentException("Seed directory must stay on the writable Render disk without symlinks.");
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = User.class)
    @EnableJpaRepositories(basePackageClasses = UserRepository.class)
    @Import({FileStorageService.class, JobServiceImpl.class})
    static class SeedConfiguration {
        @Bean
        BCryptPasswordEncoder seedPasswordEncoder() {
            return new SecurityConfig().passwordEncoder();
        }

        @Bean
        SeedRunner seedRunner(UserRepository users, org.example.repository.EmployerProfileRepository profiles,
                              org.example.repository.StoredFileRepository files, FileStorageService storage,
                              org.example.service.JobService jobs, BCryptPasswordEncoder passwords,
                              org.springframework.transaction.PlatformTransactionManager transactions) {
            return new SeedRunner(users, profiles, files, storage, jobs, passwords, transactions);
        }
    }
}
