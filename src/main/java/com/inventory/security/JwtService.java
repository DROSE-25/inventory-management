package com.inventory.security;
 
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
 
@Service
public class JwtService {
 
    @Value("${app.jwt.secret}")
    private String secret;
 
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;
 
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
 
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().stream()
            .findFirst().map(Object::toString).orElse(""));
        return Jwts.builder()
            .claims(claims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }
 
    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
            .parseSignedClaims(token).getPayload().getSubject();
    }
 
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            Date expiration = Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload().getExpiration();
            return username.equals(userDetails.getUsername()) && expiration.after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }
}
