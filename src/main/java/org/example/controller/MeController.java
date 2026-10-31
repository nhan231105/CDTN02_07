package org.example.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.DomainException;
import org.example.dto.*;
import org.example.entity.*;
import org.example.repository.*;
import org.example.security.CurrentUser;
import org.example.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;
@RestController @RequiredArgsConstructor @Transactional
public class MeController {
    private final MeProfileService profiles;private final FileStorageService files;private final StudentProfileRepository students;private final ApplicationRepository applications;
    @GetMapping("/me/profile")public Map<String,Object> profile(){return profiles.get();}
    @RequestMapping(value="/me/profile",method={RequestMethod.PUT,RequestMethod.PATCH})public Map<String,Object> update(@Valid @RequestBody MeProfileRequest request){return profiles.update(request);}
    @PatchMapping("/me/account")public Map<String,Object> account(@Valid @RequestBody AccountUpdateRequest request){return profiles.account(request);}
    @PostMapping("/me/cv")public ResponseEntity<?> uploadCv(@RequestParam("file")MultipartFile file){CurrentUser.requireRole(UserRole.STUDENT);return ResponseEntity.status(201).body(files.metadata(files.upload(CurrentUser.id(),file,false)));}
    @PostMapping("/me/avatar")public ResponseEntity<?> uploadAvatar(@RequestParam("file")MultipartFile file){CurrentUser.requireRole(UserRole.STUDENT);return ResponseEntity.status(201).body(files.metadata(files.upload(CurrentUser.id(),file,true)));}
    @GetMapping("/me/cv")public ResponseEntity<byte[]> cv(){return download(files.requireOwnedCv(files.getCurrentCv(CurrentUser.id()),CurrentUser.id()),false);}
    @GetMapping("/me/avatar")public ResponseEntity<byte[]> avatar(){var p=students.findById(CurrentUser.id()).orElseThrow();return download(files.requireOwned(p.getAvatarRef(),CurrentUser.id()),true);}
    @GetMapping("/applications/{id}/cv")public ResponseEntity<byte[]> applicationCv(@PathVariable Long id){
        var app=applications.findById(id).orElseThrow(()->new DomainException(404,"APPLICATION_NOT_FOUND","Không tìm thấy đơn"));
        Long me=CurrentUser.id();if(!app.getStudent().getId().equals(me)&&!(CurrentUser.role()==UserRole.EMPLOYER&&app.getJob().getEmployer().getId().equals(me)))throw new DomainException(403,"FORBIDDEN","Bạn không có quyền tải CV này");
        return download(files.requireOwnedCv(app.getCvFileRef(),app.getStudent().getId()),false);
    }
    private ResponseEntity<byte[]> download(StoredFile f,boolean inline){return ResponseEntity.ok().contentType(MediaType.parseMediaType(f.getContentType())).header(HttpHeaders.CONTENT_DISPOSITION,(inline?ContentDisposition.inline():ContentDisposition.attachment()).filename(f.getOriginalName(),StandardCharsets.UTF_8).build().toString()).header(HttpHeaders.CACHE_CONTROL,"no-store").header("X-Content-Type-Options","nosniff").body(files.read(f));}
}
