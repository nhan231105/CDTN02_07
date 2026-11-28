package campuswork.seed;

import org.example.entity.*;
import org.example.repository.EmployerProfileRepository;
import org.example.repository.StoredFileRepository;
import org.example.repository.UserRepository;
import org.example.service.FileStorageService;
import org.example.service.JobService;
import org.example.security.CurrentUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static campuswork.seed.SeedCatalog.DEMO_LABEL;
import static campuswork.seed.SeedCatalog.DEMO_NOTICE;

final class SeedRunner {
    private final UserRepository users;
    private final EmployerProfileRepository profiles;
    private final StoredFileRepository files;
    private final FileStorageService storage;
    private final JobService jobs;
    private final BCryptPasswordEncoder passwords;
    private final TransactionTemplate transaction;
    private final SecureRandom random = new SecureRandom();

    SeedRunner(UserRepository users, EmployerProfileRepository profiles, StoredFileRepository files,
               FileStorageService storage, JobService jobs, BCryptPasswordEncoder passwords,
               PlatformTransactionManager transactionManager) {
        this.users = users;
        this.profiles = profiles;
        this.files = files;
        this.storage = storage;
        this.jobs = jobs;
        this.passwords = passwords;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    Result apply(List<SeedCatalog.Company> companies, LocalDate today, Path exportDirectory) {
        return apply(companies, today, exportDirectory, null, false);
    }

    int preview(List<SeedCatalog.Company> companies, Path uploads) {
        var readOnly = new TransactionTemplate(transaction.getTransactionManager());
        readOnly.setReadOnly(true);
        return readOnly.execute(status -> plan(companies, uploads).conflicts());
    }

    Result apply(List<SeedCatalog.Company> companies, LocalDate today, Path exportDirectory,
                 Path uploads, boolean strict) {
        return transaction.execute(status -> {
            var plan = plan(companies, uploads);
            if (strict && plan.conflicts() > 0) {
                throw new IllegalArgumentException("Resolve all flagged conflicts before production apply.");
            }
            var pending = plan.pending();
            if (pending.isEmpty()) return new Result(0, 0, companies.size(), null, null);

            var credentials = pending.stream().map(company -> new Credential(company, newPassword())).toList();
            var accounts = new ArrayList<String>();
            accounts.add("company_key,email,phone,password");
            for (var entry : credentials) {
                var company = entry.company();
                accounts.add(String.join(",", company.key(), company.email(), company.phone(), entry.password()));
            }
            Path pendingCsv = prepareFile(exportDirectory, "accounts-", accounts);
            Path finalCsv = pendingCsv.resolveSibling(pendingCsv.getFileName().toString().replace(".pending", ""));
            publishOnCommit(pendingCsv, finalCsv);
            var receipt = new ArrayList<String>();
            receipt.add("company_key,user_id,logo_ref,job_ids");
            for (var credential : credentials) {
                receipt.add(insertCompany(credential, today));
            }
            Path pendingReceipt = prepareFile(exportDirectory, "receipt-", receipt);
            Path finalReceipt = finalCsv.resolveSibling(finalCsv.getFileName().toString().replace("accounts-", "receipt-"));
            publishOnCommit(pendingReceipt, finalReceipt);
            int jobCount = pending.stream().mapToInt(company -> company.jobs().size()).sum();
            return new Result(pending.size(), jobCount, companies.size() - pending.size(), finalCsv, finalReceipt);
        });
    }

    private Plan plan(List<SeedCatalog.Company> companies, Path uploads) {
        var pending = new ArrayList<SeedCatalog.Company>();
        int conflicts = 0;
        for (var company : companies) {
            String ref = company.logoRef().toString();
            var file = files.findById(ref).orElse(null);
            if (file != null) {
                boolean expected = file.getOwner().getRole() == UserRole.EMPLOYER
                        && file.getOriginalName().equals(company.fileName())
                        && file.getStorageName().equals(ref + ".png")
                        && (storage.hasStoredImage(ref) || uploads != null
                            && Files.isRegularFile(uploads.resolve(file.getStorageName()), LinkOption.NOFOLLOW_LINKS));
                System.out.printf("%s %s%n", company.key(), expected ? "already seeded" : "conflict: logo metadata or file");
                if (!expected) conflicts++;
            } else if (users.existsByEmail(company.email()) || users.existsByPhone(company.phone())
                    || uploads != null && Files.exists(uploads.resolve(ref + ".png"), LinkOption.NOFOLLOW_LINKS)) {
                System.out.printf("%s conflict: existing email, phone or logo filename%n", company.key());
                conflicts++;
            } else {
                pending.add(company);
            }
        }
        System.out.printf("Plan: %d new companies, %d new jobs, %d conflicts.%n", pending.size(),
                pending.stream().mapToInt(company -> company.jobs().size()).sum(), conflicts);
        return new Plan(pending, conflicts);
    }

    private static void publishOnCommit(Path pending, Path destination) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    Files.move(pending, destination);
                } catch (IOException e) {
                    System.err.println("Database committed. Retrieve the private pending file at " + pending);
                }
            }

            @Override
            public void afterCompletion(int completion) {
                if (completion != STATUS_ROLLED_BACK) return;
                try {
                    Files.deleteIfExists(pending);
                } catch (IOException e) {
                    System.err.println("Could not remove private rolled-back file at " + pending);
                }
            }
        });
    }

    private String insertCompany(Credential credential, LocalDate today) {
        var company = credential.company();
        var user = new User();
        user.setFullName(company.fullName());
        user.setEmail(company.email());
        user.setPhone(company.phone());
        user.setRole(UserRole.EMPLOYER);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(passwords.encode(credential.password()));
        var saved = users.saveAndFlush(user);
        var profile = new EmployerProfile();
        profile.setUser(saved);
        profile.setOrgName(DEMO_LABEL + company.orgName());
        profile.setAddress(company.location());
        profile.setDescription(DEMO_NOTICE + company.description());
        profile.setIndustry(company.industry());
        profile.setCompanySize("Nhóm demo");
        profile.setTagline("Công ty giả lập CampusWork");
        profile.setBenefits(company.benefits());
        profiles.saveAndFlush(profile);
        storage.uploadLogo(saved.getId(), new Logo(company.fileName(), company.png()), company.logoRef());

        // Run the same authorized job creation path as the employer controller.
        var previous = SecurityContextHolder.getContext();
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentUser.Principal(saved.getId(), UserRole.EMPLOYER), null,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYER"))));
        SecurityContextHolder.setContext(context);
        var jobIds = new ArrayList<String>();
        try {
            for (var listing : company.jobs()) {
                var job = jobs.createJob(listing.request(company, today), saved.getId());
                jobIds.add(job.getId().toString());
            }
        } finally {
            SecurityContextHolder.setContext(previous);
        }
        return String.join(",", company.key(), saved.getId().toString(), company.logoRef().toString(),
                String.join(";", jobIds));
    }

    private String newPassword() {
        var bytes = new byte[24];
        random.nextBytes(bytes);
        return "Cw!" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static Path prepareFile(Path directory, String prefix, List<String> lines) {
        Path pending = null;
        try {
            Files.createDirectories(directory);
            if (Files.getFileStore(directory).supportsFileAttributeView("posix")) {
                pending = Files.createTempFile(directory, prefix, ".csv.pending",
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
            } else {
                pending = Files.createTempFile(directory, prefix, ".csv.pending");
            }
            Files.write(pending, lines, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return pending;
        } catch (IOException e) {
            if (pending != null) {
                try {
                    Files.deleteIfExists(pending);
                } catch (IOException cleanup) {
                    e.addSuppressed(cleanup);
                }
            }
            throw new UncheckedIOException("Cannot write private credentials export", e);
        }
    }

    record Result(int companies, int jobs, int skipped, Path credentials, Path receipt) {}
    private record Plan(List<SeedCatalog.Company> pending, int conflicts) {}
    private record Credential(SeedCatalog.Company company, String password) {}

    private record Logo(String fileName, byte[] bytes) implements MultipartFile {
        @Override public String getName() { return "file"; }
        @Override public String getOriginalFilename() { return fileName; }
        @Override public String getContentType() { return "image/png"; }
        @Override public boolean isEmpty() { return bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() { return bytes; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
        @Override public void transferTo(java.io.File destination) throws IOException {
            Files.write(destination.toPath(), bytes);
        }
    }
}
