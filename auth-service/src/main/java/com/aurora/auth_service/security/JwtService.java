package com.aurora.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey key;

    // Spec'e göre secret key application.yml üzerinden enjekte ediliyor[cite: 1]
    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Token'ın içinden müşteri ID'sini (subject) okuma
    public String extractCustomerId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Token'dan istenilen herhangi bir veriyi okuyan jenerik metot
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Spec'e göre sub=customerId, claim="email", exp=1 saat olacak şekilde token oluşturma[cite: 1]
    public String generateToken(Long customerId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(customerId)) // JWT standardında subject string olmalıdır
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 saat geçerli[cite: 1]
                .signWith(key)
                .compact();
    }

    // Gelen token'ın bu müşteriye ait olup olmadığını ve süresini doğrulama
    public boolean isTokenValid(String token, Long customerId) {
        final String extractedId = extractCustomerId(token);
        return (extractedId.equals(String.valueOf(customerId))) && !isTokenExpired(token);
    }

    // Token'ın süresinin dolup dolmadığını kontrol etme
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Token'ın bitiş tarihini çekme
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Şifreyi çözüp token'ın içindeki tüm verilere (Payload) ulaşma
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}