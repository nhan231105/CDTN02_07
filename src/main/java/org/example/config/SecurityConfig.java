package org.example.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.ApiExceptionHandler;
import org.example.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.*;
@Configuration
public class SecurityConfig {
 @Bean public BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins:http://localhost:5173,http://127.0.0.1:5173}")String origins){
  var c=new CorsConfiguration();c.setAllowedOrigins(Arrays.asList(origins.split(",")));c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));c.setExposedHeaders(List.of("Content-Disposition"));
  var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",c);return source;
 }
 @Bean public SecurityFilterChain filterChain(HttpSecurity http,JwtAuthenticationFilter jwt,ObjectMapper mapper,@org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource") CorsConfigurationSource cors)throws Exception{
  return http.cors(c->c.configurationSource(cors)).csrf(c->c.disable())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a
    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers("/auth/admin/**","/admin/**").hasRole("ADMIN")
    .requestMatchers("/auth/login","/auth/register","/auth/health","/health").permitAll()
    .requestMatchers(HttpMethod.POST,"/reports").hasAnyRole("STUDENT","EMPLOYER")
    .requestMatchers(HttpMethod.GET,"/jobs/employer/**","/jobs/*/applications").hasRole("EMPLOYER")
    .requestMatchers(HttpMethod.GET,"/jobs","/jobs/search","/jobs/*","/reviews/**").permitAll()
    .requestMatchers(HttpMethod.POST,"/jobs/*/applications").hasRole("STUDENT")
    .requestMatchers(HttpMethod.POST,"/jobs").hasRole("EMPLOYER")
    .requestMatchers(HttpMethod.PUT,"/jobs/**").hasRole("EMPLOYER")
    .requestMatchers(HttpMethod.PATCH,"/jobs/**").hasRole("EMPLOYER")
    .requestMatchers("/me/cv","/me/avatar","/me/applications").hasRole("STUDENT")
    .anyRequest().authenticated())
   .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{res.setStatus(401);res.setContentType("application/json;charset=UTF-8");mapper.writeValue(res.getOutputStream(),ApiExceptionHandler.body("UNAUTHENTICATED","Vui lòng đăng nhập"));})
    .accessDeniedHandler((req,res,ex)->{res.setStatus(403);res.setContentType("application/json;charset=UTF-8");mapper.writeValue(res.getOutputStream(),ApiExceptionHandler.body("FORBIDDEN","Bạn không có quyền thực hiện thao tác này"));}))
   .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
 }
}
