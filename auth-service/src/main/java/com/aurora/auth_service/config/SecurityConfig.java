package com.aurora.auth_service.config;

import com.aurora.auth_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // Şifreleri hashlemek ve kontrol etmek için BCrypt encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // REST API için CSRF kapalı

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Kayıt Ol ve Giriş Yap rotaları herkese açık
                        .requestMatchers("/actuator/health").permitAll() // Health check açık olmalı
                        .anyRequest().authenticated() // Diğer TÜM rotalar Token isteyecek
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Oturum yok, her istekte token kontrol edilecek

                // Kapıdaki güvenlik görevlimizi (Filtreyi), Spring'in standart şifre kontrolünden hemen ÖNCE çalışacak şekilde ekliyoruz
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Yetkisiz erişimlerde whitelabel error page yerine 401 JSON dönüyoruz
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                );

        return http.build();
    }
}