package org.example;

import com.fasterxml.jackson.databind.*;
import org.apache.pdfbox.pdmodel.*;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.JwtService;
import org.example.service.NotificationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:business_test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "file.upload.directory=${java.io.tmpdir}/freelance-business-tests",
    "jwt.secret=business-test-only-secret-at-least-32-bytes-long"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BusinessIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired StudentProfileRepository students;
    @Autowired EmployerProfileRepository employers;
    @Autowired JobRepository jobs;
    @Autowired ApplicationRepository applications;
    @Autowired ReviewRepository reviews;
    @Autowired NotificationRepository notifications;
    @Autowired BCryptPasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    @SpyBean NotificationService notificationService;
    static final AtomicInteger sequence=new AtomicInteger(1000);
    String prefix;
    User student,otherStudent,employer,otherEmployer;
    String studentToken,otherStudentToken,employerToken,otherEmployerToken;

    @BeforeEach void fixture() {
        prefix="business-"+UUID.randomUUID().toString().substring(0,8);
        student=user(UserRole.STUDENT,"Student A"); otherStudent=user(UserRole.STUDENT,"Student B");
        employer=user(UserRole.EMPLOYER,"Employer A"); otherEmployer=user(UserRole.EMPLOYER,"Employer B");
        studentToken=token(student); otherStudentToken=token(otherStudent); employerToken=token(employer); otherEmployerToken=token(otherEmployer);
    }
    User user(UserRole role,String name) {
        return new org.springframework.transaction.support.TransactionTemplate(transactions).execute(tx -> {
        int n=sequence.incrementAndGet(); User user=new User();
        user.setFullName(name); user.setEmail(prefix+n+"@example.test"); user.setPhone(String.format("09%08d",n));
        user.setPasswordHash(encoder.encode("Demo@12345")); user.setRole(role); user.setStatus(UserStatus.ACTIVE); user=users.saveAndFlush(user);
        if(role==UserRole.STUDENT) { StudentProfile profile=new StudentProfile(); profile.setUser(user); students.saveAndFlush(profile); }
        if(role==UserRole.EMPLOYER) { EmployerProfile profile=new EmployerProfile(); profile.setUser(user); profile.setOrgName(name); employers.saveAndFlush(profile); }
        return user;
        });
    }
    String token(User user) { return jwt.issue(user,Instant.now().plusSeconds(3600)); }
    Job job(String title) {
        Job job=new Job(); job.setEmployer(employer); job.setTitle(prefix+" "+title); job.setDescription("A sufficiently detailed job description for integration testing.");
        job.setCategory("Design"); job.setLocation("Da Nang"); job.setWorkSchedule("Evening"); job.setSalary(new BigDecimal("50000"));
        job.setDeadline(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(7)); job.setStatus(JobStatus.OPEN); return jobs.saveAndFlush(job);
    }
    byte[] pdf(String title) throws Exception {
        try(PDDocument document=new PDDocument();ByteArrayOutputStream output=new ByteArrayOutputStream()) {
            document.addPage(new PDPage()); document.getDocumentInformation().setTitle(title); document.save(output); return output.toByteArray();
        }
    }
    String upload(String token) throws Exception {
        return upload(token,pdf("CV-"+UUID.randomUUID()));
    }
    String upload(String token,byte[] contents) throws Exception {
        MvcResult result=mvc.perform(multipart("/me/cv").file(new MockMultipartFile("file","cv.pdf","application/pdf",contents)).header("Authorization","Bearer "+token)).andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).path("ref").asText();
    }
    MockHttpServletRequestBuilder applyRequest(Long jobId,String token,String cv) throws Exception {
        Map<String,Object> body=new LinkedHashMap<>();body.put("coverLetter","I would like to join this team.");body.put("consentDataUsage",true);if(cv!=null)body.put("cvFileRef",cv);
        return post("/jobs/"+jobId+"/applications").header("Authorization","Bearer "+token).contentType("application/json").content(mapper.writeValueAsBytes(body));
    }
    Long apply(Job job) throws Exception {
        upload(studentToken);
        return mapper.readTree(mvc.perform(applyRequest(job.getId(),studentToken,null)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).path("id").asLong();
    }
    MockHttpServletRequestBuilder decision(Long id,String token,String decision) {
        return patch("/applications/"+id+"/decision").header("Authorization","Bearer "+token).contentType("application/json").content("{\"decision\":\""+decision+"\"}");
    }
    MockHttpServletRequestBuilder transition(Long id,String token,String state) {
        return patch("/jobs/"+id+"/status").header("Authorization","Bearer "+token).contentType("application/json").content("{\"status\":\""+state+"\"}");
    }
    MockHttpServletRequestBuilder review(Long id,String token) {
        return post("/applications/"+id+"/reviews").header("Authorization","Bearer "+token).contentType("application/json").content("{\"rating\":5,\"content\":\"Clear brief and reliable employer.\"}");
    }

    @Test void publicSearchCombinesEveryFilterAndCapsPagesAtTwelve() throws Exception {
        for(int i=0;i<15;i++) job("Matching "+i);
        Job wrongCategory=job("WrongCategory");wrongCategory.setCategory("Marketing");jobs.saveAndFlush(wrongCategory);
        Job wrongLocation=job("WrongLocation");wrongLocation.setLocation("Ha Noi");jobs.saveAndFlush(wrongLocation);
        Job wrongSalary=job("WrongSalary");wrongSalary.setSalary(new BigDecimal("100000"));jobs.saveAndFlush(wrongSalary);
        Job wrongSchedule=job("WrongSchedule");wrongSchedule.setWorkSchedule("Morning");jobs.saveAndFlush(wrongSchedule);
        Job closed=job("Closed");closed.setStatus(JobStatus.FILLED);jobs.saveAndFlush(closed);
        Job completed=job("Completed");completed.setStatus(JobStatus.COMPLETED);jobs.saveAndFlush(completed);
        Job removed=job("Removed");removed.setRemovedByAdmin(true);jobs.saveAndFlush(removed);
        Job expired=job("Expired");expired.setDeadline(LocalDate.now().minusDays(1));jobs.saveAndFlush(expired);
        var request=get("/jobs").param("keyword",prefix).param("category","Design").param("location","Da Nang").param("minSalary","40000").param("maxSalary","60000").param("workSchedule","Evening").param("size","99");
        mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(15)).andExpect(jsonPath("$.content.length()").value(12)).andExpect(jsonPath("$.size").value(12));
        mvc.perform(get("/jobs").param("keyword",prefix).param("category","Design").param("location","Da Nang").param("minSalary","40000").param("maxSalary","60000").param("workSchedule","Evening").param("page","1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(3));
        mvc.perform(get("/jobs/"+closed.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/jobs/"+removed.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/jobs").param("minSalary","100").param("maxSalary","1")).andExpect(status().isBadRequest());
    }
    @Test void applyPersistsContactCvAndHistoryAndRejectsDuplicate() throws Exception {
        Job job=job("Application");Long id=apply(job);
        mvc.perform(applyRequest(job.getId(),studentToken,null)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ALREADY_APPLIED"));
        mvc.perform(get("/me/applications").header("Authorization","Bearer "+studentToken)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id)).andExpect(jsonPath("$.content[0].jobTitle").value(job.getTitle())).andExpect(jsonPath("$.content[0].reviewSubmitted").value(false));
        Application saved=applications.findById(id).orElseThrow();assertThat(saved.getFullName()).isEqualTo(student.getFullName());assertThat(saved.getCvFileRef()).isNotBlank();assertThat(saved.getConsentDataUsage()).isTrue();
    }
    @Test void applyRejectsAnonymousWrongRoleForeignCvAndMissingConsent() throws Exception {
        Job job=job("Security");String foreign=upload(otherStudentToken);
        mvc.perform(post("/jobs/"+job.getId()+"/applications").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(applyRequest(job.getId(),employerToken,foreign)).andExpect(status().isForbidden());
        mvc.perform(applyRequest(job.getId(),studentToken,foreign)).andExpect(status().isForbidden());
        mvc.perform(post("/jobs/"+job.getId()+"/applications").header("Authorization","Bearer "+studentToken).contentType("application/json").content("{\"consentDataUsage\":false}")).andExpect(status().isBadRequest());
        mvc.perform(post("/jobs/"+job.getId()+"/applications").header("Authorization","Bearer "+studentToken).contentType("application/json").content("{\"consentDataUsage\":true,\"studentId\":"+otherStudent.getId()+"}")).andExpect(status().isForbidden());
    }
    @Test void applyRejectsClosedExpiredAndRemovedJobs() throws Exception {
        upload(studentToken);
        Job closed=job("Closed");closed.setStatus(JobStatus.FILLED);jobs.saveAndFlush(closed);
        Job expired=job("Expired");expired.setDeadline(LocalDate.now().minusDays(1));jobs.saveAndFlush(expired);
        Job removed=job("Removed");removed.setRemovedByAdmin(true);jobs.saveAndFlush(removed);
        for(Job job:List.of(closed,expired,removed))mvc.perform(applyRequest(job.getId(),studentToken,null)).andExpect(status().isConflict());
    }
    @Test void concurrentDuplicateSubmissionCreatesExactlyOneRow() throws Exception {
        Job job=job("ConcurrentApply");upload(studentToken);
        List<Integer> results=concurrently(() -> mvc.perform(applyRequest(job.getId(),studentToken,null)).andReturn().getResponse().getStatus(),() -> mvc.perform(applyRequest(job.getId(),studentToken,null)).andReturn().getResponse().getStatus());
        assertThat(results).containsExactlyInAnyOrder(201,409);assertThat(applications.countByJobIdAndStudentId(job.getId(),student.getId())).isEqualTo(1);
    }
    @Test void employerOwnershipAndLifecycleAreEnforced() throws Exception {
        Job job=job("Lifecycle");Long id=apply(job);
        mvc.perform(get("/jobs/"+job.getId()+"/applications").header("Authorization","Bearer "+otherEmployerToken)).andExpect(status().isForbidden());
        mvc.perform(decision(id,otherEmployerToken,"APPROVED")).andExpect(status().isForbidden());
        mvc.perform(transition(job.getId(),employerToken,"COMPLETED")).andExpect(status().isConflict());
        mvc.perform(transition(job.getId(),employerToken,"FILLED")).andExpect(status().isConflict());
        mvc.perform(decision(id,employerToken,"APPROVED")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(decision(id,employerToken,"REJECTED")).andExpect(status().isConflict());
        mvc.perform(transition(job.getId(),otherEmployerToken,"FILLED")).andExpect(status().isForbidden());
        mvc.perform(transition(job.getId(),employerToken,"FILLED")).andExpect(status().isOk());
        mvc.perform(transition(job.getId(),employerToken,"COMPLETED")).andExpect(status().isOk());
        mvc.perform(get("/jobs/"+job.getId()).header("Authorization","Bearer "+studentToken)).andExpect(status().isOk());
    }
    @Test void simultaneousApproveRejectHasOneDecisionAndOneStudentNotification() throws Exception {
        Job job=job("ConcurrentDecision");Long id=apply(job);
        List<Integer> results=concurrently(() -> mvc.perform(decision(id,employerToken,"APPROVED")).andReturn().getResponse().getStatus(),() -> mvc.perform(decision(id,employerToken,"REJECTED")).andReturn().getResponse().getStatus());
        assertThat(results).containsExactlyInAnyOrder(200,409);assertThat(notifications.countByUserIdAndStatus(student.getId(),NotificationStatus.UNREAD)).isEqualTo(1);
    }
    @Test void notificationFailureRollsBackApprovalAndDoesNotCreateNotification() throws Exception {
        Job job=job("Rollback");Long id=apply(job);
        doThrow(new IllegalStateException("Injected notification failure")).when(notificationService).createNotification(eq(student.getId()),anyString(),eq("APPLICATION"),eq(id));
        mvc.perform(decision(id,employerToken,"APPROVED")).andExpect(status().isInternalServerError());
        assertThat(applications.findById(id).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(notifications.countByUserIdAndStatus(student.getId(),NotificationStatus.UNREAD)).isZero();
    }
    @Test void reviewsRequireOwnedApprovedCompletedApplicationAndAreUnique() throws Exception {
        Job job=job("Review");Long id=apply(job);
        mvc.perform(review(id,otherStudentToken)).andExpect(status().isForbidden());
        mvc.perform(review(id,studentToken)).andExpect(status().isConflict());
        mvc.perform(decision(id,employerToken,"APPROVED")).andExpect(status().isOk());
        mvc.perform(review(id,studentToken)).andExpect(status().isConflict());
        mvc.perform(transition(job.getId(),employerToken,"FILLED")).andExpect(status().isOk());
        mvc.perform(transition(job.getId(),employerToken,"COMPLETED")).andExpect(status().isOk());
        mvc.perform(post("/applications/"+id+"/reviews").header("Authorization","Bearer "+studentToken).contentType("application/json").content("{\"rating\":6,\"content\":\"Invalid rating\"}")).andExpect(status().isBadRequest());
        mvc.perform(review(id,studentToken)).andExpect(status().isCreated()).andExpect(jsonPath("$.rating").value(5));
        mvc.perform(review(id,studentToken)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ALREADY_REVIEWED"));
        mvc.perform(get("/me/applications").header("Authorization","Bearer "+studentToken)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].reviewSubmitted").value(true));
        mvc.perform(get("/reviews/users/"+employer.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test void notificationsArePrivatePersistentAndReadIsIdempotent() throws Exception {
        Job job=job("Inbox");Long id=apply(job);mvc.perform(decision(id,employerToken,"REJECTED")).andExpect(status().isOk());
        JsonNode inbox=mapper.readTree(mvc.perform(get("/me/notifications").header("Authorization","Bearer "+studentToken)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        long notification=inbox.path("content").get(0).path("id").asLong();
        mvc.perform(get("/me/notifications/count/unread").header("Authorization","Bearer "+studentToken)).andExpect(content().string("1"));
        mvc.perform(get("/me/notifications/"+notification).header("Authorization","Bearer "+otherStudentToken)).andExpect(status().isForbidden());
        mvc.perform(patch("/me/notifications/"+notification+"/read").header("Authorization","Bearer "+otherStudentToken)).andExpect(status().isForbidden());
        mvc.perform(get("/me/notifications").param("userId",student.getId().toString()).header("Authorization","Bearer "+otherStudentToken)).andExpect(status().isForbidden());
        for(int attempt=0;attempt<2;attempt++)mvc.perform(patch("/me/notifications/"+notification+"/read").header("Authorization","Bearer "+studentToken)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("READ"));
        mvc.perform(get("/me/notifications/count/unread").header("Authorization","Bearer "+studentToken)).andExpect(content().string("0"));
    }
    @Test void replacingProfileCvKeepsOriginalApplicationSnapshot() throws Exception {
        Job job=job("Snapshot");byte[] first=pdf("Original CV");String original=upload(studentToken,first);
        Long id=mapper.readTree(mvc.perform(applyRequest(job.getId(),studentToken,null)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).path("id").asLong();
        String replacement=upload(studentToken,pdf("Replacement CV"));assertThat(replacement).isNotEqualTo(original);
        mvc.perform(get("/applications/"+id+"/cv").header("Authorization","Bearer "+studentToken)).andExpect(status().isOk()).andExpect(content().bytes(first));
        mvc.perform(get("/applications/"+id+"/cv").header("Authorization","Bearer "+employerToken)).andExpect(status().isOk()).andExpect(content().bytes(first));
        mvc.perform(get("/applications/"+id+"/cv").header("Authorization","Bearer "+otherEmployerToken)).andExpect(status().isForbidden());
    }
    @Test void jobsCannotBeEditedAfterFirstApplicationAndInvalidFieldsAreRejected() throws Exception {
        Job job=job("Immutable");apply(job);
        mvc.perform(put("/jobs/"+job.getId()).header("Authorization","Bearer "+employerToken).contentType("application/json").content("{\"salary\":99999}")).andExpect(status().isConflict());
        mvc.perform(post("/jobs").header("Authorization","Bearer "+employerToken).contentType("application/json").content("{\"title\":\"New work\",\"description\":\"Description long enough for validation\",\"category\":\"Design\",\"location\":\"Da Nang\",\"salary\":1000,\"workSchedule\":\"Evening\"}")).andExpect(status().isBadRequest());
    }
    @Test void normalizedJobFieldsCannotBeBlankOrShorterThanMinimum() throws Exception {
        Job job=job("TrimValidation");
        for(Map<String,String> body:List.of(Map.of("title","     "),Map.of("title","    A"),Map.of("description"," ".repeat(20)),Map.of("description"," ".repeat(19)+"A"),Map.of("category","   "),Map.of("location","   "),Map.of("workSchedule","   "))) {
            mvc.perform(put("/jobs/"+job.getId()).header("Authorization","Bearer "+employerToken).contentType("application/json").content(mapper.writeValueAsBytes(body)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_JOB_FIELD"));
        }
        assertThat(jobs.findById(job.getId()).orElseThrow().getTitle()).isEqualTo(job.getTitle());
        Map<String,Object> create=new LinkedHashMap<>();create.put("title","    A");create.put("description","A valid description with sufficient characters.");create.put("category","Design");create.put("location","Da Nang");create.put("workSchedule","Evening");create.put("salary",50000);create.put("deadline",LocalDate.now().plusDays(7).toString());
        mvc.perform(post("/jobs").header("Authorization","Bearer "+employerToken).contentType("application/json").content(mapper.writeValueAsBytes(create)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_JOB_FIELD"));
    }
    @Test void removedJobCannotBeApproved() throws Exception {
        Job job=job("Moderated");Long id=apply(job);job.setRemovedByAdmin(true);jobs.saveAndFlush(job);
        mvc.perform(decision(id,employerToken,"APPROVED")).andExpect(status().isConflict());
        assertThat(applications.findById(id).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }
    @Test void rejectedApplicationCannotReviewEvenAfterJobCompleted() throws Exception {
        Job job=job("RejectedReview");Long id=apply(job);
        mvc.perform(decision(id,employerToken,"REJECTED")).andExpect(status().isOk());
        job.setStatus(JobStatus.COMPLETED);jobs.saveAndFlush(job);
        mvc.perform(review(id,studentToken)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("APPLICATION_NOT_APPROVED"));
    }
    @Test void concurrentReviewSubmissionCreatesExactlyOneReview() throws Exception {
        Job job=job("ConcurrentReview");Long id=apply(job);
        mvc.perform(decision(id,employerToken,"APPROVED")).andExpect(status().isOk());
        mvc.perform(transition(job.getId(),employerToken,"FILLED")).andExpect(status().isOk());
        mvc.perform(transition(job.getId(),employerToken,"COMPLETED")).andExpect(status().isOk());
        List<Integer> results=concurrently(() -> mvc.perform(review(id,studentToken)).andReturn().getResponse().getStatus(),() -> mvc.perform(review(id,studentToken)).andReturn().getResponse().getStatus());
        assertThat(results).containsExactlyInAnyOrder(201,409);
    }
    @SafeVarargs final List<Integer> concurrently(Callable<Integer>... calls) throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(calls.length);CountDownLatch gate=new CountDownLatch(1);
        try { List<Future<Integer>> futures=new ArrayList<>();for(Callable<Integer> call:calls)futures.add(pool.submit(() -> {gate.await();return call.call();}));gate.countDown();List<Integer> result=new ArrayList<>();for(Future<Integer> future:futures)result.add(future.get(30,TimeUnit.SECONDS));return result; }
        finally { pool.shutdownNow(); }
    }
}
