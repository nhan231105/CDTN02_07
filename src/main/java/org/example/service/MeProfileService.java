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
        var u=users.findById(CurrentUser.id()).orElseThrow();Map<String,Object> m=new LinkedHashMap<>();
        m.put("id",u.getId());m.put("fullName",u.getFullName());m.put("email",u.getEmail());m.put("phone",u.getPhone());m.put("role",u.getRole().name());m.put("status",u.getStatus().name());
        if(u.getRole()==UserRole.STUDENT){var p=students.findById(u.getId()).orElseThrow();m.put("avatarRef",p.getAvatarRef());m.put("bio",p.getBio());m.put("cvFileRef",p.getCvFileRef());m.put("cvOriginalName",p.getCvOriginalName());m.put("cvSize",p.getCvFileRef()==null?null:files.findById(p.getCvFileRef()).map(StoredFile::getSize).orElse(null));}
        if(u.getRole()==UserRole.EMPLOYER){var p=employers.findById(u.getId()).orElseThrow();m.put("orgName",p.getOrgName());m.put("address",p.getAddress());m.put("description",p.getDescription());}
        return m;
    }
    public Map<String,Object> update(MeProfileRequest request){
        var u=users.findById(CurrentUser.id()).orElseThrow();if(request.getFullName()!=null){if(request.getFullName().trim().length()<3)throw new DomainException(400,"INVALID_NAME","Họ tên cần ít nhất 3 ký tự");u.setFullName(request.getFullName().trim());}
        if(u.getRole()==UserRole.STUDENT){var p=students.findById(u.getId()).orElseThrow();if(request.getBio()!=null)p.setBio(request.getBio());}
        if(u.getRole()==UserRole.EMPLOYER){var p=employers.findById(u.getId()).orElseThrow();if(request.getOrgName()!=null)p.setOrgName(request.getOrgName());if(request.getAddress()!=null)p.setAddress(request.getAddress());if(request.getDescription()!=null)p.setDescription(request.getDescription());}
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
