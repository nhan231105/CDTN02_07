package campuswork.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.example.dto.CreateJobRequest;
import org.example.entity.JobShift;
import org.example.entity.JobType;
import org.example.entity.PayType;
import org.example.entity.UserRole;
import org.example.service.RegisterRequest;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

final class SeedCatalog {
    private static final String ROOT = "/campuswork-seed/";
    static final String DEMO_LABEL = "[DEMO] ";
    static final String DEMO_NOTICE = "Dữ liệu mô phỏng CampusWork. Đây không phải thông tin tuyển dụng thật. ";

    private SeedCatalog() {}

    static List<Company> load(LocalDate today) throws IOException, NoSuchAlgorithmException {
        return load(today, Path.of("db", "seed-assets", "logos"));
    }

    static List<Company> load(LocalDate today, Path logoDirectory) throws IOException, NoSuchAlgorithmException {
        List<Company> companies;
        try (var manifest = resource("companies.json")) {
            companies = new ObjectMapper().readValue(manifest, new TypeReference<>() {});
        }

        require(companies.size() == 15, "Expected 15 companies");
        require(companies.stream().mapToInt(company -> company.jobs().size()).sum() == 40, "Expected 40 jobs");
        var validated = new ArrayList<Company>();
        var companyKeys = new HashSet<String>();
        var logoDigests = new HashSet<String>();
        var jobTitles = new HashSet<String>();
        var sha256 = MessageDigest.getInstance("SHA-256");
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (Company company : companies) {
                require(company.key().matches("cw(0[1-9]|1[0-5])") && companyKeys.add(company.key()),
                        "Invalid or duplicate company key");
                require(company.jobs().size() >= 2 && company.jobs().size() <= 3, company.key() + ": job count");
                require(company.fieldsValid(), company.key() + ": incomplete profile");
                var sampleAccount = new RegisterRequest(company.fullName(), company.email(), company.phone(),
                        "Validation!Example", UserRole.EMPLOYER.name());
                require(validator.validate(sampleAccount).isEmpty(), company.key() + ": invalid account template");
                var logoPath = logoDirectory.resolve(company.fileName());
                require(Files.isRegularFile(logoPath) && Files.size(logoPath) <= 2 * 1024 * 1024,
                        company.key() + ": missing or oversized PNG in " + logoDirectory);
                var bytes = Files.readAllBytes(logoPath);
                require(bytes.length >= 8 && bytes.length <= 2 * 1024 * 1024 &&
                        HexFormat.of().formatHex(bytes, 0, 8).equals("89504e470d0a1a0a"),
                        company.key() + ": invalid PNG");
                var image = ImageIO.read(new ByteArrayInputStream(bytes));
                require(image != null && image.getWidth() == 512 && image.getHeight() == 512,
                        company.key() + ": logo must be 512×512");
                require(logoDigests.add(HexFormat.of().formatHex(sha256.digest(bytes))), company.key() + ": duplicate logo");
                for (Listing listing : company.jobs()) {
                    require(jobTitles.add(listing.title()), company.key() + ": duplicate title");
                    require(listing.deadlineDays() >= 30 && listing.deadlineDays() <= 60,
                            company.key() + ": invalid deadline offset");
                    require(listing.salary() > 0 && present(listing.requirements(), 2000)
                            && present(listing.benefits(), 1000), company.key() + ": incomplete job");
                    require(validator.validate(listing.request(company, today)).isEmpty(),
                            company.key() + ": job does not match CreateJobRequest");
                }
                validated.add(new Company(company.key(), company.fullName(), company.orgName(), company.industry(),
                        company.location(), company.description(), company.benefits(), company.jobs(), bytes));
            }
        }
        return List.copyOf(validated);
    }

    private static InputStream resource(String name) {
        return Objects.requireNonNull(SeedCatalog.class.getResourceAsStream(ROOT + name), "Missing seed asset: " + name);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private static boolean present(String value, int maxLength) {
        return value != null && !value.isBlank() && value.length() <= maxLength;
    }

    record Company(String key, String fullName, String orgName, String industry, String location,
                   String description, String benefits, List<Listing> jobs, byte[] png) {
        String email() { return key + "@campuswork-seed.example"; }
        String phone() { return "00000000" + key.substring(2); }
        String fileName() { return key + ".png"; }
        UUID logoRef() {
            return UUID.nameUUIDFromBytes(("campuswork-demo-v1/logo/" + key).getBytes(StandardCharsets.UTF_8));
        }

        boolean fieldsValid() {
            return present(orgName, 150 - DEMO_LABEL.length()) && present(industry, 100)
                    && present(location, 100) && present(description, 3000 - DEMO_NOTICE.length())
                    && present(benefits, 600);
        }
    }

    record Listing(String title, long salary, JobShift shift, int hours, String schedule, String description,
                   String requirements, String benefits, int deadlineDays) {
        CreateJobRequest request(Company company, LocalDate today) {
            var request = new CreateJobRequest();
            request.setTitle(DEMO_LABEL + title);
            request.setCategory(company.industry());
            request.setLocation(company.location());
            request.setDescription(DEMO_NOTICE + description);
            request.setRequirements(requirements);
            request.setBenefits(benefits);
            request.setSalary(BigDecimal.valueOf(salary));
            request.setJobType(JobType.PART_TIME.name());
            request.setPayType(PayType.HOURLY.name());
            request.setWorkSchedule(schedule);
            request.setShifts(List.of(shift.name()));
            request.setHoursPerWeek(hours);
            request.setDeadline(today.plusDays(deadlineDays));
            return request;
        }
    }
}
