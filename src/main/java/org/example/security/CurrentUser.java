package org.example.security;
import org.example.common.DomainException;
import org.example.entity.UserRole;
import org.springframework.security.core.context.SecurityContextHolder;
public final class CurrentUser {
    private CurrentUser() { }
    public record Principal(Long id,UserRole role) { }
    private static Principal principal() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !(auth.getPrincipal() instanceof Principal p)) throw new DomainException(401,"UNAUTHENTICATED","Vui lòng đăng nhập"); return p;
    }
    public static Long id(){return principal().id();}
    public static UserRole role(){return principal().role();}
    public static void requireId(Long supplied){if(supplied!=null&&!id().equals(supplied))throw new DomainException(403,"FORBIDDEN","Bạn không có quyền truy cập dữ liệu này");}
    public static void requireRole(UserRole role){if(role()!=role)throw new DomainException(403,"FORBIDDEN","Vai trò không được phép thực hiện thao tác này");}
}
