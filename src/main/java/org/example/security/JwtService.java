package org.example.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.example.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
@Service
public class JwtService {
    private final SecretKey key; private final long expiration;
    public JwtService(@Value("${jwt.secret}")String secret,@Value("${jwt.expiration:3600000}")long expiration){
        if(secret.getBytes(StandardCharsets.UTF_8).length<32)throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiration=expiration;
    }
    public Instant expiresAt(){return Instant.now().plusMillis(expiration);}
    public String issue(User user,Instant expiresAt){return Jwts.builder().subject(user.getId().toString()).claim("ver",user.getTokenVersion()).issuedAt(new Date()).expiration(Date.from(expiresAt)).signWith(key).compact();}
    public Claims parse(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();}
}
