package com.aurora.order_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component

public class jwtAuthFilter extends OncePerRequestFilter {

    private final SecretKey key;

    // Spec'e göre auth-service ile tamamen aynı JWT_SECRET değişkenini okur
    public jwtAuthFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. İstekte token yoksa diğer filtrelere geçir (SecurityConfig 401 fırlatacak)
        if (authHeader == null || !authHeader.startsWith("Bearer") ) {
            filterChain.doFilter(request , response);
            return;
        }

        try {
            // 2. Token'ı al ve imzayı auth-service ile aynı şifreyle doğrula
            final String jwt = authHeader.substring(7);
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            // 3. Spec kuralı: JWT'nin "sub" (subject) alanı customerId'yi taşır
            Long customerId = Long.parseLong(claims.getSubject());

            // 4. Kimliği Spring Security Context'ine yerleştir (Artık Controller bu ID'ye ulaşabilir)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        customerId,
                        null,
                        new java.util.ArrayList<>()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Token bozuksa, süresi geçmişse veya yanlış imzalanmışsa hiçbir şey yapmıyoruz.
            // Context boş kaldığı için SecurityConfig aşağıdaki adımda otomatik 401 dönecek.
        }

        filterChain.doFilter(request, response);
    }
}
