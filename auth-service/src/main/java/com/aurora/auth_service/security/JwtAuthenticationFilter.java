package com.aurora.auth_service.security;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull jakarta.servlet.http.HttpServletRequest request,
            @NonNull jakarta.servlet.http.HttpServletResponse response,
            @NonNull jakarta.servlet.FilterChain filterChain
    ) throws jakarta.servlet.ServletException, java.io.IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Header yoksa veya Bearer ile başlamıyorsa diğer filtrelere yolla (kimliksiz istek)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. "Bearer " kısmını atıp sadece Token'ı alıyoruz (artık çakışma yok)
        final String jwt = authHeader.substring(7);

        // 3. Token'ın içinden ID (subject) çıkarıyoruz
        final String customerIdStr = jwtService.extractCustomerId(jwt);

        // 4. ID var ama sistemde henüz bu istek için giriş (Authentication) yapılmamışsa:
        if (customerIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            Long customerId = Long.parseLong(customerIdStr);

            // 5. MİKROSERVİS KURALI: Veritabanına (UserDetailsService) gitmiyoruz!
            // Sadece token'ın süresi ve imzası geçerli mi ona bakıyoruz.
            if (jwtService.isTokenValid(jwt, customerId)) {

                // Spring Security'ye "Bu kişiyi içeri al, kimliğini doğruladım" diyoruz.
                // Principal (kimlik) olarak e-posta değil, doğrudan customerId (Long) veriyoruz.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        customerId,
                        null,
                        new java.util.ArrayList<>() // Yetkiler şimdilik boş
                );

                authToken.setDetails(
                        new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}