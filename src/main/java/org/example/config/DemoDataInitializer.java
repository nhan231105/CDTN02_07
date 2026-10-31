package org.example.config;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.*;
import org.example.entity.*;
import org.example.repository.*;
import org.example.service.FileStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Component @Profile({"dev-h2","h2"}) @RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.demo.seed",havingValue="true",matchIfMissing=true)
public class DemoDataInitializer implements CommandLineRunner {
    private final UserRepository users;private final StudentProfileRepository students;private final EmployerProfileRepository employers;
    private final JobRepository jobs;private final ApplicationRepository applications;private final NotificationRepository notifications;private final ReviewRepository reviews;
    private final BCryptPasswordEncoder passwords;private final FileStorageService files;
    @Override @Transactional public void run(String... args)throws Exception{
        if(users.count()>0)return;
        User studentA=user("Minh Anh Nguyễn","student.a@demo.local","0900000001",UserRole.STUDENT);
        User studentB=user("Hoàng Bảo Trần","student.b@demo.local","0900000002",UserRole.STUDENT);
        User employerA=user("Lotus Creative Studio","employer.a@demo.local","0900000003",UserRole.EMPLOYER);
        User employerB=user("Bright Future Vietnam","employer.b@demo.local","0900000004",UserRole.EMPLOYER);
        user("Quản trị viên Demo","admin@demo.local","0900000005",UserRole.ADMIN);
        try(var pdf=new PDDocument();var output=new ByteArrayOutputStream()){
            pdf.addPage(new PDPage());pdf.save(output);byte[] bytes=output.toByteArray();
            files.upload(studentA.getId(),new DemoFile(bytes),false);files.upload(studentB.getId(),new DemoFile(bytes),false);
        }
        String[] titles={"Thiết kế giao diện website","Lập trình React bán thời gian","Trợ lý nội dung sáng tạo","Chuyên viên Marketing online","Thiết kế bộ nhận diện thương hiệu","Phát triển ứng dụng Java","Biên tập video mạng xã hội","Lập trình website thương mại","Trợ lý nghiên cứu thị trường","Thiết kế poster sự kiện","Phát triển API Spring Boot","Cộng tác viên truyền thông"};
        String[] categories={"Design","Development","Marketing"};String[] locations={"TP. Hồ Chí Minh","Hà Nội","Đà Nẵng"};
        List<Job> created=new ArrayList<>();
        for(int i=0;i<24;i++){
            Job job=new Job();job.setEmployer(i%2==0?employerA:employerB);job.setTitle(titles[i%titles.length]+(i>=12?" • Dự án "+(i+1):""));
            job.setCategory(categories[i%3]);job.setLocation(locations[(i/3)%3]);job.setSalary(BigDecimal.valueOf(1500000L+(i%8)*500000L));
            job.setDescription("Tham gia dự án thực tế cùng đội ngũ trẻ, được hướng dẫn và phát triển kỹ năng chuyên môn. Công việc phù hợp sinh viên, thời gian linh hoạt và quy trình cộng tác rõ ràng.");
            job.setRequirements("Chủ động, có trách nhiệm và mong muốn học hỏi. Có sản phẩm cá nhân là một lợi thế.");job.setBenefits("Lịch linh hoạt, hỗ trợ chuyên môn, xác nhận kinh nghiệm và cơ hội cộng tác lâu dài.");
            job.setWorkSchedule(i%2==0?"Linh hoạt":"Bán thời gian");job.setDeadline(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(30));
            job.setStatus(i<16?JobStatus.OPEN:i<20?JobStatus.FILLED:JobStatus.COMPLETED);created.add(jobs.save(job));
        }
        Application pending=application(created.get(0),studentB,ApplicationStatus.PENDING);
        Application approved=application(created.get(20),studentA,ApplicationStatus.APPROVED);
        application(created.get(21),studentA,ApplicationStatus.APPROVED);
        application(created.get(1),studentA,ApplicationStatus.REJECTED);
        for(int i=16;i<20;i++)application(created.get(i),studentA,ApplicationStatus.APPROVED);
        Notification n=new Notification();n.setUser(studentA);n.setMessage("Hồ sơ của bạn đã được duyệt. Chúc bạn có trải nghiệm làm việc tốt!");n.setRelatedType("APPLICATION");n.setRelatedId(approved.getId());notifications.save(n);
        Notification read=new Notification();read.setUser(studentA);read.setMessage("Chào mừng bạn đến với cộng đồng việc làm sinh viên.");read.setStatus(NotificationStatus.READ);read.setRelatedType("APPLICATION");read.setRelatedId(approved.getId());notifications.save(read);
        Notification company=new Notification();company.setUser(employerA);company.setMessage("Bạn có hồ sơ ứng tuyển mới cần xem xét.");company.setRelatedType("APPLICATION");company.setRelatedId(pending.getId());notifications.save(company);
        Review review=new Review();review.setJob(approved.getJob());review.setReviewer(studentA);review.setReviewee(approved.getJob().getEmployer());review.setRating(5);review.setContent("Môi trường chuyên nghiệp, hướng dẫn tận tình và lịch làm phù hợp sinh viên.");reviews.save(review);
    }
    private User user(String name,String email,String phone,UserRole role){
        User u=new User();u.setFullName(name);u.setEmail(email);u.setPhone(phone);u.setPasswordHash(passwords.encode("DemoPass123!"));u.setRole(role);u=users.saveAndFlush(u);
        if(role==UserRole.STUDENT){StudentProfile p=new StudentProfile();p.setUser(u);p.setBio("Sinh viên yêu thích học hỏi, sáng tạo và các dự án có giá trị thực tế.");students.saveAndFlush(p);}
        if(role==UserRole.EMPLOYER){EmployerProfile p=new EmployerProfile();p.setUser(u);p.setOrgName(name);p.setAddress("TP. Hồ Chí Minh");p.setDescription("Đội ngũ sáng tạo kết nối sinh viên với những cơ hội phát triển thực tế.");employers.saveAndFlush(p);}return u;
    }
    private Application application(Job job,User student,ApplicationStatus status){Application a=new Application();a.setJob(job);a.setStudent(student);a.setFullName(student.getFullName());a.setEmail(student.getEmail());a.setPhone(student.getPhone());a.setCoverLetter("Tôi mong muốn được tham gia và đóng góp cho dự án.");a.setConsentDataUsage(true);a.setCvFileRef(files.getCurrentCv(student.getId()));a.setStatus(status);if(status!=ApplicationStatus.PENDING)a.setDecidedAt(LocalDateTime.now());return applications.save(a);}
    private record DemoFile(byte[] bytes) implements MultipartFile{
        public String getName(){return "file";}public String getOriginalFilename(){return "CV-demo.pdf";}public String getContentType(){return "application/pdf";}public boolean isEmpty(){return false;}public long getSize(){return bytes.length;}public byte[] getBytes(){return bytes;}public InputStream getInputStream(){return new ByteArrayInputStream(bytes);}public void transferTo(File target)throws IOException{try(var out=new FileOutputStream(target)){out.write(bytes);}}
    }
}
