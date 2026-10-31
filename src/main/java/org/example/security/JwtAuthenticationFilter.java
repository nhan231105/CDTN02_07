package org.example.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.example.common.ApiExceptionHandler;
import org.example.entity.UserStatus;
import org.example.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt; private final UserRepository users; private final ObjectMapper mapper;
    public JwtAuthenticationFilter(JwtService jwt,UserRepository users,ObjectMapper mapper){this.jwt=jwt;this.users=users;this.mapper=mapper;}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
        String authorization=request.getHeader("Authorization");
        if(authorization!=null){try{
            if(!authorization.startsWith("Bearer "))throw new IllegalArgumentException();
            var claims=jwt.parse(authorization.substring(7));var user=users.findById(Long.valueOf(claims.getSubject())).orElseThrow();
            if(user.getStatus()!=UserStatus.ACTIVE){write(response,403,"ACCOUNT_LOCKED","Tài khoản đã bị khóa");return;}
            if(claims.get("ver",Integer.class)!=user.getTokenVersion())throw new IllegalArgumentException();
            var principal=new CurrentUser.Principal(user.getId(),user.getRole());
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal,null,List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()))));
        }catch(Exception e){SecurityContextHolder.clearContext();write(response,401,"INVALID_TOKEN","Phiên đăng nhập không hợp lệ hoặc đã hết hạn");return;}}
        chain.doFilter(request,response);
    }
    private void write(HttpServletResponse response,int status,String code,String message)throws IOException{response.setStatus(status);response.setContentType("application/json;charset=UTF-8");mapper.writeValue(response.getOutputStream(),ApiExceptionHandler.body(code,message));}
}
