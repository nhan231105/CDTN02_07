package org.example.service;
import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.*;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.CurrentUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional
public class MeProfileService {
    private final UserRepository users;private final StudentProfileRepository students;private final EmployerProfileRepository employers;private final StoredFileRepository files;private final BCryptPasswordEncoder passwords;
    public Map<String,Object> get(){
        var u=users.findById(CurrentUser.id()).orElseThrow();Map<String,Object> m=new LinkedHashMap<>();List<String> missing=new ArrayList<>();int total=0;
        m.put("id",u.getId());m.put("fullName",u.getFullName());m.put("email",u.getEmail());m.put("phone",u.getPhone());m.put("role",u.getRole().name());m.put("status",u.getStatus().name());
        if(u.getRole()==UserRole.STUDENT){var p=students.findById(u.getId()).orElseThrow();m.put("avatarRef",p.getAvatarRef());m.put("bio",p.getBio());m.put("cvFileRef",p.getCvFileRef());m.put("cvOriginalName",p.getCvOriginalName());m.put("cvSize",p.getCvFileRef()==null?null:files.findById(p.getCvFileRef()).map(StoredFile::getSize).orElse(null));
            m.put("school",p.getSchool());m.put("major",p.getMajor());m.put("studyYear",p.getStudyYear());m.put("skills",p.getSkills());m.put("availableShifts",split(p.getAvailableShifts()));m.put("preferredArea",p.getPreferredArea());m.put("openToWork",p.isOpenToWork());m.put("lat",p.getLat());m.put("lng",p.getLng());
            m.put("examMode",p.isExamMode());m.put("examStartDate",p.getExamStartDate());m.put("examEndDate",p.getExamEndDate());m.put("examModeActive",examModeActive(p));
            check(missing,p.getAvatarRef(),"Ảnh đại diện");check(missing,p.getBio(),"Giới thiệu bản thân");check(missing,p.getCvFileRef(),"CV (PDF)");check(missing,p.getSchool(),"Trường đang học");check(missing,p.getMajor(),"Ngành học");check(missing,p.getStudyYear(),"Năm học");check(missing,p.getSkills(),"Kỹ năng");check(missing,p.getAvailableShifts(),"Ca có thể làm");check(missing,p.getPreferredArea(),"Khu vực mong muốn");total=9;}
        if(u.getRole()==UserRole.EMPLOYER){var p=employers.findById(u.getId()).orElseThrow();m.put("orgName",p.getOrgName());m.put("address",p.getAddress());m.put("description",p.getDescription());m.put("logoRef",p.getLogoRef());m.put("website",p.getWebsite());m.put("industry",p.getIndustry());m.put("companySize",p.getCompanySize());
            m.put("coverRef",p.getCoverRef());m.put("tagline",p.getTagline());m.put("foundedYear",p.getFoundedYear());m.put("benefits",benefits(p.getBenefits()));
            check(missing,p.getLogoRef(),"Logo");check(missing,p.getCoverRef(),"Ảnh bìa");check(missing,p.getTagline(),"Khẩu hiệu");check(missing,p.getFoundedYear(),"Năm thành lập");check(missing,p.getBenefits(),"Phúc lợi");check(missing,p.getOrgName(),"Tên doanh nghiệp");check(missing,p.getAddress(),"Địa chỉ");check(missing,p.getDescription(),"Giới thiệu doanh nghiệp");check(missing,p.getWebsite(),"Website");check(missing,p.getIndustry(),"Lĩnh vực");check(missing,p.getCompanySize(),"Quy mô");total=12;}
        if(total>0){m.put("completeness",(total-missing.size())*100/total);m.put("missingFields",missing);}
        return m;
    }
    private static List<String> split(String csv){return csv==null||csv.isBlank()?List.of():Arrays.asList(csv.split(","));}
    // Mùa thi tự bật/tắt theo ngày: nếu có đặt khoảng ngày, chỉ thực sự "đang mùa thi" khi hôm nay nằm trong khoảng đó
    private static boolean examModeActive(StudentProfile p){
        if(!p.isExamMode())return false;
        var today=java.time.LocalDate.now();
        if(p.getExamStartDate()!=null&&today.isBefore(p.getExamStartDate()))return false;
        if(p.getExamEndDate()!=null&&today.isAfter(p.getExamEndDate()))return false;
        return true;
    }
    public static List<String> benefits(String raw){return raw==null||raw.isBlank()?List.of():Arrays.stream(raw.split("\\|")).map(String::trim).filter(s->!s.isEmpty()).toList();}
    private static String blank(String v){return v.isBlank()?null:v.trim();}
    private static void check(List<String> missing,Object value,String label){if(value==null||value.toString().isBlank())missing.add(label);}
    public Map<String,Object> update(MeProfileRequest request){
        var u=users.findById(CurrentUser.id()).orElseThrow();if(request.getFullName()!=null){if(request.getFullName().trim().length()<3)throw new DomainException(400,"INVALID_NAME","Họ tên cần ít nhất 3 ký tự");u.setFullName(request.getFullName().trim());}
        if(u.getRole()==UserRole.STUDENT){var p=students.findById(u.getId()).orElseThrow();if(request.getBio()!=null)p.setBio(request.getBio());
            if(request.getSchool()!=null)p.setSchool(blank(request.getSchool()));if(request.getMajor()!=null)p.setMajor(blank(request.getMajor()));if(request.getStudyYear()!=null)p.setStudyYear(request.getStudyYear());
            if(request.getSkills()!=null)p.setSkills(blank(request.getSkills()));if(request.getPreferredArea()!=null)p.setPreferredArea(blank(request.getPreferredArea()));if(request.getLat()!=null&&request.getLng()!=null){p.setLat(request.getLat());p.setLng(request.getLng());}
            if(request.getAvailableShifts()!=null)p.setAvailableShifts(String.join(",",new LinkedHashSet<>(request.getAvailableShifts())));
            if(request.getOpenToWork()!=null){if(request.getOpenToWork()&&(p.getAvailableShifts()==null||p.getAvailableShifts().isBlank()))throw new DomainException(400,"SHIFTS_REQUIRED","Hãy chọn ít nhất một ca có thể làm trước khi bật chế độ tìm việc");p.setOpenToWork(request.getOpenToWork());}
            if(request.getExamStartDate()!=null)p.setExamStartDate(request.getExamStartDate());if(request.getExamEndDate()!=null)p.setExamEndDate(request.getExamEndDate());
            if(p.getExamStartDate()!=null&&p.getExamEndDate()!=null&&p.getExamEndDate().isBefore(p.getExamStartDate()))throw new DomainException(400,"INVALID_EXAM_RANGE","Ngày kết thúc mùa thi phải sau ngày bắt đầu");
            if(request.getExamMode()!=null)p.setExamMode(request.getExamMode());}
        if(u.getRole()==UserRole.EMPLOYER){var p=employers.findById(u.getId()).orElseThrow();if(request.getOrgName()!=null)p.setOrgName(request.getOrgName());if(request.getAddress()!=null)p.setAddress(request.getAddress());if(request.getDescription()!=null)p.setDescription(request.getDescription());
            if(request.getWebsite()!=null)p.setWebsite(blank(request.getWebsite()));if(request.getIndustry()!=null)p.setIndustry(blank(request.getIndustry()));if(request.getCompanySize()!=null)p.setCompanySize(blank(request.getCompanySize()));
            if(request.getTagline()!=null)p.setTagline(blank(request.getTagline()));if(request.getFoundedYear()!=null)p.setFoundedYear(request.getFoundedYear());if(request.getBenefits()!=null)p.setBenefits(blank(request.getBenefits()));}
        return get();
    }
    public Map<String,Object> account(AccountUpdateRequest request){
        var u=users.findLockedById(CurrentUser.id()).orElseThrow();if(!passwords.matches(request.getCurrentPassword(),u.getPasswordHash()))throw new DomainException(403,"INVALID_PASSWORD","Mật khẩu hiện tại chưa đúng");
        if(u.getContactChangedAt()!=null&&u.getContactChangedAt().plusHours(48).isAfter(LocalDateTime.now()))throw new DomainException(409,"ACCOUNT_CHANGE_LIMIT","Chỉ được đổi thông tin tài khoản một lần trong 48 giờ");
        if(request.getEmail()==null&&request.getPhone()==null&&request.getPassword()==null)throw new DomainException(400,"EMPTY_UPDATE","Không có thông tin cần thay đổi");
        if(request.getEmail()!=null){String email=request.getEmail().trim().toLowerCase(Locale.ROOT);if(email.isBlank())throw new DomainException(400,"INVALID_EMAIL","Email không hợp lệ");if(!email.equals(u.getEmail())&&users.existsByEmail(email))throw new DomainException(409,"DUPLICATE_EMAIL","Email đã được đăng ký");u.setEmail(email);}
        if(request.getPhone()!=null){if(!request.getPhone().equals(u.getPhone())&&users.existsByPhone(request.getPhone()))throw new DomainException(409,"DUPLICATE_PHONE","Số điện thoại đã được đăng ký");u.setPhone(request.getPhone());}
        if(request.getPassword()!=null){u.setPasswordHash(passwords.encode(request.getPassword()));u.setTokenVersion(u.getTokenVersion()+1);}
        u.setContactChangedAt(LocalDateTime.now());users.saveAndFlush(u);return Map.of("message","Đã cập nhật tài khoản","requiresLogin",request.getPassword()!=null);
    }
}
