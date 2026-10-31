package org.example.service;
import org.apache.pdfbox.Loader;
import org.example.common.DomainException;
import org.example.entity.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;
import java.util.*;
@Service
public class FileStorageService {
    private final StoredFileRepository files; private final UserRepository users; private final StudentProfileRepository profiles; private final Path root;
    public FileStorageService(StoredFileRepository files,UserRepository users,StudentProfileRepository profiles,@Value("${file.upload.directory:./uploads}")String directory){this.files=files;this.users=users;this.profiles=profiles;this.root=Path.of(directory).toAbsolutePath().normalize();}
    @Transactional
    public StoredFile upload(Long owner,MultipartFile file,boolean avatar){
        if(file.isEmpty())throw new DomainException(400,"EMPTY_FILE","Vui lòng chọn tệp không rỗng");
        if(file.getSize()>(avatar?2:5)*1024*1024)throw new DomainException(413,"FILE_TOO_LARGE",avatar?"Ảnh tối đa 2 MiB":"CV tối đa 5 MiB");
        byte[] data;try{data=file.getBytes();}catch(IOException e){throw new DomainException(400,"FILE_READ_ERROR","Không đọc được tệp");}
        String type=file.getContentType();
        try{
            if(avatar){
                boolean png=data.length>8 && data[0]==(byte)137 && data[1]==80 && data[2]==78 && data[3]==71;
                boolean jpg=data.length>3 && data[0]==(byte)255 && data[1]==(byte)216 && data[2]==(byte)255;
                if(!(png&&"image/png".equals(type)||jpg&&"image/jpeg".equals(type)))throw new IOException();
                try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(data))){
                    var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IOException();var reader=readers.next();
                    try{reader.setInput(input);if(reader.getWidth(0)>4096||reader.getHeight(0)>4096)throw new IOException();if(reader.read(0)==null)throw new IOException();}finally{reader.dispose();}
                }
            }else{
                if(!"application/pdf".equals(type)||data.length<5||!new String(data,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))throw new IOException();
                try(var pdf=Loader.loadPDF(data)){if(pdf.isEncrypted()||pdf.getNumberOfPages()<1)throw new IOException();}
            }
        }catch(Exception e){throw new DomainException(415,"INVALID_FILE_TYPE",avatar?"Chỉ nhận ảnh PNG/JPEG hợp lệ":"Chỉ nhận PDF hợp lệ, không mã hóa");}
        String ref=UUID.randomUUID().toString();String name=ref+(avatar?("image/png".equals(type)?".png":".jpg"):".pdf");Path target=root.resolve(name);
        try{Files.createDirectories(root);Files.write(target,data,StandardOpenOption.CREATE_NEW);}catch(IOException e){throw new DomainException(500,"FILE_STORAGE_ERROR","Không thể lưu tệp");}
        if(TransactionSynchronizationManager.isSynchronizationActive())TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)try{Files.deleteIfExists(target);}catch(IOException ignored){}}});
        StoredFile stored=new StoredFile();stored.setRef(ref);stored.setOwner(users.findById(owner).orElseThrow());
        String original=Optional.ofNullable(file.getOriginalFilename()).orElse(name).replace('\\','/');original=original.substring(original.lastIndexOf('/')+1).replaceAll("[\\p{Cntrl}]","");
        stored.setOriginalName(original.isBlank()?name:original.substring(0,Math.min(original.length(),255)));stored.setContentType(type);stored.setSize(data.length);stored.setStorageName(name);files.save(stored);
        var profile=profiles.findById(owner).orElseThrow(()->new DomainException(404,"PROFILE_NOT_FOUND","Không tìm thấy hồ sơ"));
        if(avatar)profile.setAvatarRef(ref);else{profile.setCvFileRef(ref);profile.setCvOriginalName(stored.getOriginalName());}profiles.save(profile);return stored;
    }
    @Transactional(readOnly=true)
    public StoredFile requireOwnedCv(String ref,Long ownerId){
        if(ref==null||ref.isBlank())throw new DomainException(400,"CV_REQUIRED","Vui lòng tải CV trước khi ứng tuyển");
        var f=files.findById(ref).orElseThrow(()->new DomainException(404,"FILE_NOT_FOUND","Không tìm thấy tệp"));
        if(!f.getOwner().getId().equals(ownerId))throw new DomainException(403,"FORBIDDEN","CV không thuộc tài khoản của bạn");
        if(!"application/pdf".equals(f.getContentType()))throw new DomainException(415,"INVALID_CV","CV phải là PDF");return f;
    }
    public String getCurrentCv(Long userId){return profiles.findById(userId).map(StudentProfile::getCvFileRef).orElse(null);}
    @Transactional(readOnly=true)
    public StoredFile requireOwned(String ref,Long owner){var f=files.findById(Optional.ofNullable(ref).orElse("")).orElseThrow(()->new DomainException(404,"FILE_NOT_FOUND","Chưa có tệp"));if(!f.getOwner().getId().equals(owner))throw new DomainException(403,"FORBIDDEN","Tệp không thuộc tài khoản của bạn");return f;}
    public byte[] read(StoredFile file){Path path=root.resolve(file.getStorageName()).normalize();if(!path.startsWith(root))throw new DomainException(403,"INVALID_PATH","Đường dẫn không hợp lệ");try{return Files.readAllBytes(path);}catch(IOException e){throw new DomainException(404,"FILE_NOT_FOUND","Không tìm thấy tệp đã lưu");}}
    public Map<String,Object> metadata(StoredFile f){return Map.of("ref",f.getRef(),"originalName",f.getOriginalName(),"size",f.getSize(),"contentType",f.getContentType());}
}
