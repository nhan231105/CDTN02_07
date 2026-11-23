package org.example;

import com.fasterxml.jackson.databind.*;
import org.apache.pdfbox.pdmodel.*;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.cors.origins=https://preview.example, https://campuswork-ui.example")
@AutoConfigureMockMvc @ActiveProfiles("test")
class SecurityProfileIntegrationTest {
    @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired UserRepository users;@Autowired StudentProfileRepository students;
    @org.springframework.boot.test.mock.mockito.MockBean org.example.service.CampusEmail mail;
    @Autowired EmailChallengeRepository challenges;
    @Autowired EmployerProfileRepository employers;@Autowired BCryptPasswordEncoder passwords;@Autowired JwtService jwt;
    @Test
    void productionCorsAcceptsConfiguredOriginsAndRejectsOthers() throws Exception {
        mvc.perform(options("/me/profile")
                .header("Origin", "https://campuswork-ui.example")
                .header("Access-Control-Request-Method", "PATCH")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "https://campuswork-ui.example"));

        mvc.perform(options("/me/profile")
                .header("Origin", "https://other.example")
                .header("Access-Control-Request-Method", "PATCH"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void adminEndpointsRequireAdminRole() throws Exception {
        mvc.perform(get("/auth/admin/stats")).andExpect(status().isUnauthorized());
        String studentToken = student();
        mvc.perform(get("/admin/jobs").header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isForbidden());

        var admin = new User();
        var fixture = data("ADMIN");
        admin.setFullName(fixture.get("fullName"));
        admin.setEmail(fixture.get("email"));
        admin.setPhone(fixture.get("phone"));
        admin.setPasswordHash(passwords.encode(fixture.get("password")));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        String adminToken = jwt.issue(users.saveAndFlush(admin), jwt.expiresAt());

        mvc.perform(get("/auth/admin/stats").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
        mvc.perform(get("/admin/jobs").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test
    void renderRequiresAnExplicitJwtSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtService("", 60_000, true));
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 60_000, true));
        assertDoesNotThrow(() -> new JwtService("", 60_000, false));
    }

    private String body(Object value)throws Exception{return json.writeValueAsString(value);}
    private Map<String,String> data(String role){String key=UUID.randomUUID().toString().substring(0,8);return Map.of("fullName","Test User "+key,"email",key+"@test.local","phone","09"+String.format("%08d",Math.floorMod(UUID.randomUUID().hashCode(),100000000)),"password","GoodPass123!","role",role);}
    private JsonNode register(Map<String,String> data)throws Exception{
        var code = requestCode("/auth/register", data, data.get("email"));
        return json.readTree(mvc.perform(post("/auth/register/verify").contentType("application/json")
                .content(body(Map.of("registration", data, "code", code))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("data");
    }
    private String requestCode(String path, Map<String,String> data, String email)throws Exception{
        var code = org.mockito.ArgumentCaptor.forClass(String.class);
        mvc.perform(post(path).contentType("application/json").content(body(data))).andExpect(status().isAccepted());
        org.mockito.Mockito.verify(mail).send(org.mockito.ArgumentMatchers.eq(email.toLowerCase(Locale.ROOT)),
                org.mockito.ArgumentMatchers.anyString(), code.capture());
        return code.getValue();
    }
    private String login(Map<String,String> data)throws Exception{return json.readTree(mvc.perform(post("/auth/login").contentType("application/json").content(body(Map.of("identifier",data.get("email"),"password",data.get("password"))))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).at("/data/accessToken").asText();}
    private String student()throws Exception{var d=data("STUDENT");register(d);return login(d);}
    private byte[] pdf()throws Exception{try(var doc=new PDDocument();var out=new ByteArrayOutputStream()){doc.addPage(new PDPage());doc.save(out);return out.toByteArray();}}
    @Test void registrationCreatesHashedUserAndProfile()throws Exception{
        var d=data("STUDENT");var result=register(d);var u=users.findById(result.get("id").asLong()).orElseThrow();
        assertNotEquals(d.get("password"),u.getPasswordHash());assertTrue(passwords.matches(d.get("password"),u.getPasswordHash()));assertTrue(students.existsById(u.getId()));
        var e=data("EMPLOYER");assertTrue(employers.existsById(register(e).get("id").asLong()));
    }
    @Test void registrationRejectsAdminWeakPasswordAndDuplicate()throws Exception{
        mvc.perform(post("/auth/register").contentType("application/json").content(body(data("ADMIN")))).andExpect(status().isBadRequest());
        var weak=new HashMap<>(data("STUDENT"));weak.put("password","123456");mvc.perform(post("/auth/register").contentType("application/json").content(body(weak))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.password").exists());
        var d=data("STUDENT");register(d);
        mvc.perform(post("/auth/register").contentType("application/json").content(body(d))).andExpect(status().isTooManyRequests());
    }
    @Test void registrationHidesDuplicateEmailOrPhoneWithoutCallingProvider() throws Exception {
        var existing = data("STUDENT");
        register(existing);
        org.mockito.Mockito.clearInvocations(mail);

        var challenge = challenges.findById("register:" + existing.get("email")).orElseThrow();
        challenge.setSentAt(Instant.now().minusSeconds(61));
        challenges.saveAndFlush(challenge);

        var duplicateEmail = new HashMap<>(data("EMPLOYER"));
        duplicateEmail.put("email", existing.get("email").toUpperCase(Locale.ROOT));
        var duplicatePhone = new HashMap<>(data("EMPLOYER"));
        duplicatePhone.put("phone", existing.get("phone"));
        for (var registration : List.of(duplicateEmail, duplicatePhone)) {
            mvc.perform(post("/auth/register").contentType("application/json").content(body(registration)))
                    .andExpect(status().isAccepted()).andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Nếu thông tin đăng ký hợp lệ, bạn sẽ nhận được mã xác minh."));
        }
        org.mockito.Mockito.verify(mail, org.mockito.Mockito.never()).send(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());

        var fresh = data("EMPLOYER");
        assertTrue(requestCode("/auth/register", fresh, fresh.get("email")).matches("[0-9]{6}"));
    }

    @Test void otpExpiresAndResendObsoletesPreviousCode() throws Exception {
        var d = data("STUDENT");
        String email = d.get("email");
        String code = requestCode("/auth/register", d, email);
        assertFalse(users.existsByEmail(email));
        mvc.perform(post("/auth/login").contentType("application/json")
                .content(body(Map.of("identifier", email, "password", d.get("password")))))
                .andExpect(status().isUnauthorized());
        var entry = challenges.findById("register:" + email).orElseThrow();
        assertNotEquals(code, entry.getCodeHash());
        mvc.perform(post("/auth/register").contentType("application/json").content(body(d)))
                .andExpect(status().isTooManyRequests());
        entry.setSentAt(Instant.now().minusSeconds(301));
        challenges.saveAndFlush(entry);
        mvc.perform(post("/auth/register/verify").contentType("application/json")
                .content(body(Map.of("registration", d, "code", code)))).andExpect(status().isBadRequest());
        org.mockito.Mockito.clearInvocations(mail);
        String current = requestCode("/auth/register", d, email);
        assertNotEquals(code, current);
        mvc.perform(post("/auth/register/verify").contentType("application/json")
                .content(body(Map.of("registration", d, "code", code)))).andExpect(status().isBadRequest());
        String wrong = current.equals("000000") ? "111111" : "000000";
        for (int attempt = 0; attempt < 4; attempt++) {
            mvc.perform(post("/auth/register/verify").contentType("application/json")
                    .content(body(Map.of("registration", d, "code", wrong)))).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/auth/register/verify").contentType("application/json")
                .content(body(Map.of("registration", d, "code", current)))).andExpect(status().isBadRequest());
        assertFalse(users.existsByEmail(email));
    }

    @Test void providerFailureReturns503AndPreventsImmediateOtpRetry() throws Exception {
        var d = data("STUDENT");
        register(d);
        var email = d.get("email");
        org.mockito.Mockito.doThrow(new org.example.service.CampusEmail.DeliveryException()).when(mail)
                .send(org.mockito.ArgumentMatchers.eq(email), org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString());
        var request = post("/auth/password/forgot").contentType("application/json")
                .content(body(Map.of("email", email)));
        mvc.perform(request).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("EMAIL_UNAVAILABLE"));
        assertNull(challenges.findById("reset:" + email).orElseThrow().getCodeHash());
        mvc.perform(request).andExpect(status().isTooManyRequests());
    }

    @Test void passwordResetConsumesCodeAndRevokesTheOldToken() throws Exception {
        var d = data("STUDENT");
        register(d);
        String token = login(d);
        String email = d.get("email");
        org.mockito.Mockito.clearInvocations(mail);
        String code = requestCode("/auth/password/forgot", Map.of("email", email), email);
        var reset = Map.of("email", email, "code", code, "password", "NewPassword123!");
        mvc.perform(post("/auth/password/reset").contentType("application/json").content(body(reset)))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/password/reset").contentType("application/json").content(body(reset)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/me/profile").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").contentType("application/json")
                .content(body(Map.of("identifier", email, "password", d.get("password"))))).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").contentType("application/json")
                .content(body(Map.of("identifier", email, "password", reset.get("password"))))).andExpect(status().isOk());
    }

    @Test void emailNormalizationPhoneLoginAndWrongCredentials()throws Exception{
        var d=new HashMap<>(data("STUDENT"));d.put("email",d.get("email").toUpperCase(Locale.ROOT));register(d);
        mvc.perform(post("/auth/login").contentType("application/json").content(body(Map.of("identifier",d.get("phone"),"password",d.get("password"))))).andExpect(status().isOk()).andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        mvc.perform(post("/auth/login").contentType("application/json").content(body(Map.of("identifier",d.get("email"),"password","WrongPass!")))).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }
    @Test void missingForgedAndExpiredTokensAreUnauthorized()throws Exception{
        mvc.perform(get("/me/profile")).andExpect(status().isUnauthorized());mvc.perform(get("/me/profile").header("Authorization","Bearer fake.token.signature")).andExpect(status().isUnauthorized());
        var d=data("STUDENT");var id=register(d).get("id").asLong();String expired=jwt.issue(users.findById(id).orElseThrow(),Instant.now().minusSeconds(60));
        mvc.perform(get("/me/profile").header("Authorization","Bearer "+expired)).andExpect(status().isUnauthorized());
    }
    @Test void whitespaceCannotBypassMinimumNameLength()throws Exception{
        var shortName=new HashMap<>(data("STUDENT"));shortName.put("fullName","  A");
        mvc.perform(post("/auth/register").contentType("application/json").content(body(shortName))).andExpect(status().isBadRequest());
        mvc.perform(put("/me/profile").header("Authorization","Bearer "+student()).contentType("application/json").content(body(Map.of("fullName","  A")))).andExpect(status().isBadRequest());
    }
    @Test void lockedAccountCannotLoginOrUsePreviouslyIssuedToken()throws Exception{
        var d=data("STUDENT");long id=register(d).get("id").asLong();String token=login(d);var u=users.findById(id).orElseThrow();u.setStatus(UserStatus.LOCKED);users.saveAndFlush(u);
        mvc.perform(get("/me/profile").header("Authorization","Bearer "+token)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
        mvc.perform(post("/auth/login").contentType("application/json").content(body(Map.of("identifier",d.get("email"),"password",d.get("password"))))).andExpect(status().isForbidden());
    }
    @Test void studentCannotAccessAdminOrImpersonateOtherProfile()throws Exception{
        String token=student();var other=data("STUDENT");long id=register(other).get("id").asLong();
        mvc.perform(get("/auth/admin/stats").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        mvc.perform(get("/profiles/students/"+id).header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        mvc.perform(patch("/profiles/students/"+id).header("Authorization","Bearer "+token).contentType("application/json").content("{\"bio\":\"attack\"}")).andExpect(status().isForbidden());
    }
    @Test void profileUpdatesPersistAndUploadPdfDownloadsExactly()throws Exception{
        String token=student();mvc.perform(put("/me/profile").header("Authorization","Bearer "+token).contentType("application/json").content(body(Map.of("fullName","Updated Student","bio","Real profile from H2")))).andExpect(status().isOk());
        mvc.perform(get("/me/profile").header("Authorization","Bearer "+token)).andExpect(jsonPath("$.bio").value("Real profile from H2"));
        byte[] bytes=pdf();mvc.perform(multipart("/me/cv").file(new MockMultipartFile("file","cv.pdf","application/pdf",bytes)).header("Authorization","Bearer "+token)).andExpect(status().isCreated()).andExpect(jsonPath("$.ref").isNotEmpty());
        mvc.perform(get("/me/cv").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(content().bytes(bytes));
    }
    @Test void fakeEmptyAndOversizePdfAreRejected()throws Exception{
        String token=student();
        mvc.perform(multipart("/me/cv").file(new MockMultipartFile("file","fake.pdf","application/pdf","%PDF-1.7 fake contents".getBytes())).header("Authorization","Bearer "+token)).andExpect(status().isUnsupportedMediaType());
        mvc.perform(multipart("/me/cv").file(new MockMultipartFile("file","empty.pdf","application/pdf",new byte[0])).header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());
        mvc.perform(multipart("/me/cv").file(new MockMultipartFile("file","big.pdf","application/pdf",new byte[5*1024*1024+1])).header("Authorization","Bearer "+token)).andExpect(status().isPayloadTooLarge());
    }
    @Test void avatarChecksContentAndPersistsImage()throws Exception{
        String token=student();var out=new ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",out);
        mvc.perform(multipart("/me/avatar").file(new MockMultipartFile("file","avatar.png","image/png",out.toByteArray())).header("Authorization","Bearer "+token)).andExpect(status().isCreated());
        mvc.perform(get("/me/avatar").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(content().bytes(out.toByteArray()));
        mvc.perform(multipart("/me/avatar").file(new MockMultipartFile("file","avatar.png","image/png","not an image".getBytes())).header("Authorization","Bearer "+token)).andExpect(status().isUnsupportedMediaType());
    }
    @Test void employerBrandingImagesArePublicAndStudentsCannotUpload()throws Exception{
        var d=data("EMPLOYER");long id=register(d).get("id").asLong();String token=login(d);var out=new ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",out);
        mvc.perform(get("/employers/"+id+"/logo")).andExpect(status().isNotFound());
        mvc.perform(multipart("/me/logo").file(new MockMultipartFile("file","logo.png","image/png",out.toByteArray())).header("Authorization","Bearer "+token)).andExpect(status().isCreated());
        mvc.perform(multipart("/me/cover").file(new MockMultipartFile("file","cover.png","image/png",out.toByteArray())).header("Authorization","Bearer "+token)).andExpect(status().isCreated());
        mvc.perform(get("/employers/"+id+"/logo")).andExpect(status().isOk()).andExpect(content().bytes(out.toByteArray()));
        mvc.perform(get("/employers/"+id+"/cover")).andExpect(status().isOk());
        mvc.perform(put("/me/profile").header("Authorization","Bearer "+token).contentType("application/json").content(body(Map.of("tagline","Cùng sinh viên lớn lên","foundedYear",2015,"benefits","Lương theo giờ|Đào tạo miễn phí")))).andExpect(status().isOk()).andExpect(jsonPath("$.benefits[1]").value("Đào tạo miễn phí"));
        mvc.perform(get("/employers/"+id+"/public")).andExpect(status().isOk()).andExpect(jsonPath("$.tagline").value("Cùng sinh viên lớn lên")).andExpect(jsonPath("$.coverRef").isNotEmpty());
        mvc.perform(multipart("/me/logo").file(new MockMultipartFile("file","logo.png","image/png",out.toByteArray())).header("Authorization","Bearer "+student())).andExpect(status().isForbidden());
    }
    @Test void talentPoolAndInvitationsFlow()throws Exception{
        var sd=data("STUDENT");long sid=register(sd).get("id").asLong();String st=login(sd);
        var ed=data("EMPLOYER");register(ed);String et=login(ed);
        mvc.perform(put("/me/profile").header("Authorization","Bearer "+st).contentType("application/json").content(body(Map.of("openToWork",true)))).andExpect(status().isBadRequest());
        mvc.perform(put("/me/profile").header("Authorization","Bearer "+st).contentType("application/json").content(body(Map.of("openToWork",true,"availableShifts",List.of("EVENING"))))).andExpect(status().isOk());
        mvc.perform(get("/talent").header("Authorization","Bearer "+st)).andExpect(status().isForbidden());
        mvc.perform(get("/talent").param("shift","EVENING").param("size","100").header("Authorization","Bearer "+et)).andExpect(status().isOk()).andExpect(jsonPath("$.content[?(@.id==" + sid + ")]").isNotEmpty());
        String job=mvc.perform(post("/jobs").header("Authorization","Bearer "+et).contentType("application/json").content(body(Map.of("title","Phục vụ ca tối","description","Mô tả công việc đủ dài để hợp lệ","category","F&B","location","Da Nang","salary",30000,"workSchedule","Evening","deadline",java.time.LocalDate.now().plusDays(30).toString())))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long jobId=json.readTree(job).get("id").asLong();
        String inv=body(Map.of("studentId",sid,"jobId",jobId,"message","Mời bạn"));
        mvc.perform(post("/invitations").header("Authorization","Bearer "+et).contentType("application/json").content(inv)).andExpect(status().is2xxSuccessful());
        mvc.perform(post("/invitations").header("Authorization","Bearer "+et).contentType("application/json").content(inv)).andExpect(status().isConflict());
        String list=mvc.perform(get("/me/invitations").header("Authorization","Bearer "+st)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(get("/invitations/sent").header("Authorization","Bearer "+et)).andExpect(status().isOk());
    }
    @Test void nearbyJobsAreSortedByDistanceAndStudentSavesLocation()throws Exception{
        var ed=data("EMPLOYER");register(ed);String et=login(ed);
        String today=java.time.LocalDate.now().plusDays(20).toString();
        for(double[] p:new double[][]{{10.80,106.70},{10.78,106.70}}){
            mvc.perform(post("/jobs").header("Authorization","Bearer "+et).contentType("application/json").content(body(Map.of("title","Gần nhà "+p[0],"description","Mô tả công việc đủ dài để hợp lệ","category","F&B","location","TP. Hồ Chí Minh","salary",30000,"workSchedule","Evening","deadline",today,"lat",p[0],"lng",p[1])))).andExpect(status().isCreated());
        }
        mvc.perform(get("/jobs/nearby").param("lat","10.7769").param("lng","106.7009").param("radiusKm","5")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Gần nhà 10.78")).andExpect(jsonPath("$.content[0].distanceKm").isNumber());
        mvc.perform(get("/jobs/nearby").param("lat","10.7769").param("lng","106.7009").param("radiusKm","1")).andExpect(status().isOk()).andExpect(jsonPath("$.content[?(@.title=='Gần nhà 10.8')]").isEmpty());
        mvc.perform(get("/jobs/nearby").param("lat","123").param("lng","106")).andExpect(status().isBadRequest());
        String st=student();
        mvc.perform(put("/me/profile").header("Authorization","Bearer "+st).contentType("application/json").content(body(Map.of("preferredArea","Quận 1, TP.HCM","lat",10.7769,"lng",106.7009)))).andExpect(status().isOk()).andExpect(jsonPath("$.lat").value(10.7769));
    }
    @Test void accountChangesRequirePasswordAndRespect48HourLimit()throws Exception{        var d=data("STUDENT");register(d);String token=login(d);
        mvc.perform(patch("/me/account").header("Authorization","Bearer "+token).contentType("application/json").content(body(Map.of("email","changed"+d.get("email"),"currentPassword","WrongPassword!")))).andExpect(status().isForbidden());
        mvc.perform(patch("/me/account").header("Authorization","Bearer "+token).contentType("application/json").content(body(Map.of("email","changed"+d.get("email"),"currentPassword",d.get("password"))))).andExpect(status().isOk());
        mvc.perform(patch("/me/account").header("Authorization","Bearer "+token).contentType("application/json").content(body(Map.of("phone","0800000000","currentPassword",d.get("password"))))).andExpect(status().isConflict());
    }
    @Test void passwordChangeInvalidatesOldToken()throws Exception{
        var d=data("STUDENT");register(d);String token=login(d);
        mvc.perform(patch("/me/account").header("Authorization","Bearer "+token).contentType("application/json").content(body(Map.of("password","AnotherPass123!","currentPassword",d.get("password"))))).andExpect(status().isOk()).andExpect(jsonPath("$.requiresLogin").value(true));
        mvc.perform(get("/me/profile").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
    @Test void concurrentAccountChangesCannotBypass48HourLimit()throws Exception{
        var d=data("STUDENT");register(d);String token=login(d);
        var start=new java.util.concurrent.CountDownLatch(1);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        try{
            java.util.List<java.util.concurrent.Future<Integer>> attempts=new ArrayList<>();
            for(int i=0;i<2;i++){
                final int attempt=i;
                attempts.add(pool.submit(()->{start.await();return mvc.perform(patch("/me/account").header("Authorization","Bearer "+token).contentType("application/json").content(body(Map.of("email","change"+attempt+d.get("email"),"currentPassword",d.get("password"))))).andReturn().getResponse().getStatus();}));
            }
            start.countDown();var statuses=new ArrayList<Integer>();for(var future:attempts)statuses.add(future.get(20,java.util.concurrent.TimeUnit.SECONDS));
            Collections.sort(statuses);assertEquals(List.of(200,409),statuses);
        }finally{pool.shutdownNow();}
    }
}
