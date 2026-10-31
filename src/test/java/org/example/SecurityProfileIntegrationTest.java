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

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class SecurityProfileIntegrationTest {
    @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired UserRepository users;@Autowired StudentProfileRepository students;
    @Autowired EmployerProfileRepository employers;@Autowired BCryptPasswordEncoder passwords;@Autowired JwtService jwt;
    private String body(Object value)throws Exception{return json.writeValueAsString(value);}
    private Map<String,String> data(String role){String key=UUID.randomUUID().toString().substring(0,8);return Map.of("fullName","Test User "+key,"email",key+"@test.local","phone","09"+String.format("%08d",Math.floorMod(UUID.randomUUID().hashCode(),100000000)),"password","GoodPass123!","role",role);}
    private JsonNode register(Map<String,String> data)throws Exception{return json.readTree(mvc.perform(post("/auth/register").contentType("application/json").content(body(data))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("data");}
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
        var d=data("STUDENT");register(d);mvc.perform(post("/auth/register").contentType("application/json").content(body(d))).andExpect(status().isConflict());
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
    @Test void accountChangesRequirePasswordAndRespect48HourLimit()throws Exception{
        var d=data("STUDENT");register(d);String token=login(d);
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
