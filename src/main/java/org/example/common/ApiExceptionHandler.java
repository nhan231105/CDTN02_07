package org.example.common;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
    public static Map<String,Object> body(String code,String message) {
        Map<String,Object> result=new LinkedHashMap<>(); result.put("code",code); result.put("message",message); result.put("timestamp",Instant.now().toString()); return result;
    }
    @ExceptionHandler({org.example.service.EmailAlreadyExistsException.class,org.example.service.PhoneAlreadyExistsException.class})
    ResponseEntity<?> duplicate(Exception e){return ResponseEntity.status(409).body(body("DUPLICATE_ACCOUNT",e.getMessage()));}
    @ExceptionHandler(org.example.service.UserNotFoundException.class)
    ResponseEntity<?> missingUser(Exception e){return ResponseEntity.status(401).body(body("INVALID_CREDENTIALS","Tên đăng nhập hoặc mật khẩu chưa đúng"));}
    @ExceptionHandler(DomainException.class)
    ResponseEntity<?> domain(DomainException e) { return ResponseEntity.status(e.getStatus()).body(body(e.getCode(),e.getMessage())); }
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    ResponseEntity<?> routeMissing(Exception e) {return ResponseEntity.status(404).body(body("NOT_FOUND","Không tìm thấy tài nguyên"));}
    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    ResponseEntity<?> partMissing(Exception e) {return ResponseEntity.badRequest().body(body("FILE_REQUIRED","Vui lòng chọn tệp"));}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        var result=body("VALIDATION_ERROR","Vui lòng kiểm tra thông tin đã nhập"); Map<String,String> fields=new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(x->fields.putIfAbsent(x.getField(),x.getDefaultMessage())); result.put("fieldErrors",fields); return ResponseEntity.badRequest().body(result);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<?> large(Exception e) {return ResponseEntity.status(413).body(body("FILE_TOO_LARGE","Tệp vượt quá dung lượng cho phép"));}
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflict(Exception e) {return ResponseEntity.status(409).body(body("DATA_CONFLICT","Dữ liệu đã tồn tại hoặc xung đột với dữ liệu hiện có"));}
    @ExceptionHandler({IllegalArgumentException.class,HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class})
    ResponseEntity<?> invalid(Exception e) {return ResponseEntity.badRequest().body(body("INVALID_REQUEST","Dữ liệu yêu cầu không hợp lệ"));}
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<?> forbidden(Exception e) {return ResponseEntity.status(403).body(body("FORBIDDEN","Bạn không có quyền thực hiện thao tác này"));}
    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unexpected(Exception e) {org.slf4j.LoggerFactory.getLogger(getClass()).error("Unhandled request error",e); return ResponseEntity.internalServerError().body(body("INTERNAL_ERROR","Không thể xử lý yêu cầu. Vui lòng thử lại"));}
}
