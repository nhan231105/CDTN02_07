package org.example;

import com.fasterxml.jackson.databind.*;
import org.apache.pdfbox.pdmodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class PartTimeFeaturesTest {
    @Autowired MockMvc mvc;@Autowired ObjectMapper json;

    private record Account(long id,String token){}
    private String body(Object v)throws Exception{return json.writeValueAsString(v);}
    private Account account(String role)throws Exception{
        String key=UUID.randomUUID().toString().substring(0,8);
        var d=Map.of("fullName","Test User "+key,"email",key+"@test.local","phone","09"+String.format("%08d",Math.floorMod(UUID.randomUUID().hashCode(),100000000)),"password","GoodPass123!","role",role);
        long id=json.readTree(mvc.perform(post("/auth/register").contentType("application/json").content(body(d))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).at("/data/id").asLong();
        String token=json.readTree(mvc.perform(post("/auth/login").contentType("application/json").content(body(Map.of("identifier",d.get("email"),"password","GoodPass123!")))).andReturn().getResponse().getContentAsString()).at("/data/accessToken").asText();
        return new Account(id,token);
    }
    private long job(Account employer,String title,Map<String,Object> extra)throws Exception{
        var m=new HashMap<String,Object>(Map.of("title",title,"description","Mô tả công việc đủ dài để hợp lệ","category","Hospitality","location","TP. Hồ Chí Minh","workSchedule","20 giờ/tuần","salary",30000,"deadline",LocalDate.now().plusDays(10).toString(),"employerId",employer.id()));
        m.putAll(extra);
        return json.readTree(mvc.perform(post("/jobs").header("Authorization","Bearer "+employer.token()).contentType("application/json").content(body(m))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
    }
    private byte[] pdf()throws Exception{try(var doc=new PDDocument();var out=new ByteArrayOutputStream()){doc.addPage(new PDPage());doc.save(out);return out.toByteArray();}}
    private String auth(Account a){return "Bearer "+a.token();}

    @Test void filtersByShiftJobTypeNoExperienceAndSorts()throws Exception{
        var emp=account("EMPLOYER");String tag="Zq"+UUID.randomUUID().toString().substring(0,6);
        job(emp,tag+" ca toi",Map.of("jobType","PART_TIME","payType","HOURLY","shifts",List.of("EVENING"),"noExperienceRequired",true,"salary",25000));
        job(emp,tag+" thoi vu",Map.of("jobType","SEASONAL","payType","DAILY","shifts",List.of("WEEKEND"),"salary",300000));
        mvc.perform(get("/jobs").param("keyword",tag).param("shift","EVENING")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/jobs").param("keyword",tag).param("jobType","SEASONAL")).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].payType").value("DAILY"));
        mvc.perform(get("/jobs").param("keyword",tag).param("noExperience","true")).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/jobs").param("keyword",tag).param("sortBy","salary_desc")).andExpect(jsonPath("$.content[0].salary").value(300000));
        mvc.perform(get("/jobs").param("keyword",tag).param("sortBy","salary_asc")).andExpect(jsonPath("$.content[0].salary").value(25000));
    }
    @Test void savedJobsLifecycleAndRoleRestriction()throws Exception{
        var emp=account("EMPLOYER");var stu=account("STUDENT");long id=job(emp,"Saved job candidate",Map.of());
        mvc.perform(put("/me/saved-jobs/"+id).header("Authorization",auth(stu))).andExpect(status().isNoContent());
        mvc.perform(put("/me/saved-jobs/"+id).header("Authorization",auth(stu))).andExpect(status().isNoContent());
        mvc.perform(get("/me/saved-jobs/ids").header("Authorization",auth(stu))).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/me/saved-jobs").header("Authorization",auth(stu))).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].available").value(true));
        mvc.perform(put("/me/saved-jobs/"+id).header("Authorization",auth(emp))).andExpect(status().isForbidden());
        mvc.perform(delete("/me/saved-jobs/"+id).header("Authorization",auth(stu))).andExpect(status().isNoContent());
        mvc.perform(get("/me/saved-jobs/ids").header("Authorization",auth(stu))).andExpect(jsonPath("$.length()").value(0));
    }
    @Test void applicationViewedWithdrawAndNotifications()throws Exception{
        var emp=account("EMPLOYER");var stu=account("STUDENT");long jobId=job(emp,"Applied job candidate",Map.of());
        mvc.perform(multipart("/me/cv").file(new MockMultipartFile("file","cv.pdf","application/pdf",pdf())).header("Authorization",auth(stu))).andExpect(status().is2xxSuccessful());
        long appId=json.readTree(mvc.perform(post("/jobs/"+jobId+"/applications").header("Authorization",auth(stu)).contentType("application/json").content(body(Map.of("consentDataUsage",true)))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(get("/jobs/"+jobId+"/applications").header("Authorization",auth(emp))).andExpect(status().isOk());
        mvc.perform(get("/applications/"+appId).header("Authorization",auth(stu))).andExpect(jsonPath("$.viewedAt").isNotEmpty());
        mvc.perform(patch("/applications/"+appId+"/withdraw").header("Authorization",auth(emp))).andExpect(status().isForbidden());
        mvc.perform(patch("/applications/"+appId+"/withdraw").header("Authorization",auth(stu))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("WITHDRAWN")).andExpect(jsonPath("$.withdrawnAt").isNotEmpty());
        mvc.perform(patch("/applications/"+appId+"/withdraw").header("Authorization",auth(stu))).andExpect(status().isConflict());
        mvc.perform(patch("/me/notifications/read-all").header("Authorization",auth(stu))).andExpect(status().isOk());
        mvc.perform(get("/me/notifications/count/unread").header("Authorization",auth(stu))).andExpect(content().string("0"));
    }
    @Test void studentAndEmployerProfileFieldsWithCompleteness()throws Exception{
        var stu=account("STUDENT");
        mvc.perform(put("/me/profile").header("Authorization",auth(stu)).contentType("application/json").content(body(Map.of("school","ĐH Kinh tế","major","Marketing","studyYear",3,"skills","Excel","preferredArea","Quận 1","availableShifts",List.of("EVENING","WEEKEND")))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.school").value("ĐH Kinh tế")).andExpect(jsonPath("$.availableShifts.length()").value(2)).andExpect(jsonPath("$.completeness").value(66)).andExpect(jsonPath("$.missingFields").isArray());
        mvc.perform(put("/me/profile").header("Authorization",auth(stu)).contentType("application/json").content(body(Map.of("studyYear",20)))).andExpect(status().isBadRequest());
        mvc.perform(put("/me/profile").header("Authorization",auth(stu)).contentType("application/json").content(body(Map.of("availableShifts",List.of("NOPE"))))).andExpect(status().isBadRequest());
    }
    @Test void publicEmployerProfileListsOpenJobsWithoutLogin()throws Exception{
        var emp=account("EMPLOYER");
        mvc.perform(put("/me/profile").header("Authorization",auth(emp)).contentType("application/json").content(body(Map.of("orgName","Quán Test","industry","F&B","website","https://example.com","companySize","10-50 nhân viên")))).andExpect(status().isOk());
        job(emp,"Public employer job",Map.of());
        mvc.perform(get("/employers/"+emp.id()+"/public")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Quán Test")).andExpect(jsonPath("$.openJobCount").value(1)).andExpect(jsonPath("$.reviewCount").value(0));
        mvc.perform(get("/employers/999999999/public")).andExpect(status().isNotFound());
    }
}
