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

@Component @Profile({"dev-h2","h2","mysql"}) @RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.demo.seed",havingValue="true",matchIfMissing=true)
public class DemoDataInitializer implements CommandLineRunner {
    private final UserRepository users;private final StudentProfileRepository students;private final EmployerProfileRepository employers;
    private final JobRepository jobs;private final ApplicationRepository applications;private final NotificationRepository notifications;private final ReviewRepository reviews;
    private final BCryptPasswordEncoder passwords;private final FileStorageService files;private final org.springframework.core.env.Environment env;
    @Override @Transactional public void run(String... args)throws Exception{
        if(env.acceptsProfiles(org.springframework.core.env.Profiles.of("mysql"))&&!env.getProperty("app.demo.seed","false").equals("true"))return;
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
        // title, category, location, address, pay type, pay, hours/week, shifts, no experience
        String[][] data={
            {"Nhân viên phục vụ quán cà phê","Hospitality","TP. Hồ Chí Minh","25 Nguyễn Huệ, Quận 1","HOURLY","28000","20","MORNING,AFTERNOON","true"},
            {"Phục vụ nhà hàng ca tối","Hospitality","TP. Hồ Chí Minh","102 Võ Văn Tần, Quận 3","HOURLY","30000","18","EVENING","true"},
            {"Thu ngân cửa hàng tiện lợi","Sales","Hà Nội","18 Cầu Giấy","HOURLY","27000","24","AFTERNOON,EVENING","true"},
            {"Nhân viên bán hàng cuối tuần","Sales","Đà Nẵng","Vincom Ngô Quyền, Sơn Trà","SHIFT","180000","12","WEEKEND","true"},
            {"Gia sư Toán lớp 9","Education","Hà Nội","Khu đô thị Mỹ Đình","HOURLY","150000","6","EVENING,WEEKEND","false"},
            {"Gia sư Tiếng Anh giao tiếp","Education","TP. Hồ Chí Minh","Quận Bình Thạnh","HOURLY","180000","8","EVENING","false"},
            {"Trợ giảng lớp lập trình thiếu nhi","Education","Đà Nẵng","Hải Châu","SHIFT","200000","8","WEEKEND","false"},
            {"Cộng tác viên Content mạng xã hội","Marketing","TP. Hồ Chí Minh","Làm việc từ xa","PROJECT","3000000","10","FLEXIBLE","false"},
            {"Thiết kế poster, banner part-time","Design","Hà Nội","Làm việc từ xa","PROJECT","2500000","12","FLEXIBLE","false"},
            {"Quay dựng video TikTok cho quán","Marketing","TP. Hồ Chí Minh","Quận Phú Nhuận","PROJECT","4000000","10","FLEXIBLE","false"},
            {"Lập trình web React bán thời gian","Development","TP. Hồ Chí Minh","Làm việc từ xa","MONTHLY","6000000","20","FLEXIBLE","false"},
            {"Hỗ trợ kiểm thử phần mềm (QA)","Development","Hà Nội","Tòa nhà FPT, Cầu Giấy","MONTHLY","4500000","16","AFTERNOON","false"},
            {"Phát tờ rơi và PG sự kiện","Sales","Đà Nẵng","Công viên APEC","DAILY","300000","8","WEEKEND","true"},
            {"Nhân viên đóng gói đơn hàng online","Sales","TP. Hồ Chí Minh","Kho Tân Bình","HOURLY","26000","20","MORNING,EVENING","true"},
            {"Trực page và chốt đơn","Marketing","Hà Nội","Làm việc từ xa","HOURLY","25000","15","EVENING","true"},
            {"Pha chế trà sữa","Hospitality","Đà Nẵng","Lê Duẩn, Hải Châu","HOURLY","27000","20","AFTERNOON,EVENING","true"}
        };
        List<Job> created=new ArrayList<>();
        for(int i=0;i<24;i++){
            String[] d=data[i%data.length];
            Job job=new Job();job.setEmployer(i%2==0?employerA:employerB);job.setTitle(d[0]+(i>=data.length?" (đợt "+(i/data.length+1)+")":""));
            job.setCategory(d[1]);job.setLocation(d[2]);job.setAddress(d[3]);job.setPayType(PayType.valueOf(d[4]));job.setSalary(new BigDecimal(d[5]));
            job.setJobType(JobType.PART_TIME);job.setHoursPerWeek(Integer.valueOf(d[6]));job.setShifts(d[7]);job.setNoExperienceRequired(Boolean.parseBoolean(d[8]));
            job.setStudentFriendly(true);job.setHeadcount(1+i%3);job.setStartDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(7));job.setDurationMonths(3+i%4);
            job.setDescription("Công việc phù hợp sinh viên, được hướng dẫn từ đầu, lịch làm linh hoạt theo thời khóa biểu. Môi trường thân thiện, trả lương đúng hẹn.");
            job.setRequirements("Sinh viên các trường đại học, cao đẳng; nghiêm túc, đúng giờ và có tinh thần học hỏi.");job.setBenefits("Lương đúng hạn, được đào tạo, xác nhận kinh nghiệm và cơ hội làm lâu dài.");
            job.setWorkSchedule(d[7].contains("FLEXIBLE")?"Linh hoạt":d[6]+" giờ/tuần");job.setDeadline(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(30));
            job.setStatus(i<16?JobStatus.OPEN:i<20?JobStatus.FILLED:JobStatus.COMPLETED);created.add(jobs.save(job));
        }        Application pending=application(created.get(0),studentB,ApplicationStatus.PENDING);
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
