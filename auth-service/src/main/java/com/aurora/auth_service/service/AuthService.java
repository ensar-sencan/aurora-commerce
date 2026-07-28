package com.aurora.auth_service.service;

import com.aurora.auth_service.domain.Customer;
import com.aurora.auth_service.repo.CustomerRepository;
import com.aurora.auth_service.security.JwtService;
import com.aurora.auth_service.web.dto.LoginRequest;
import com.aurora.auth_service.web.dto.RegisterRequest;
import com.aurora.auth_service.web.dto.LoginResponse;
import com.aurora.auth_service.web.dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // existsByEmail ile daha performanslı kontrol
        if (customerRepository.existsByEmail(request.email())) {
            throw new RuntimeException("email_taken");
        }

        Customer customer = new Customer();
        customer.setEmail(request.email());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));

        Customer savedCustomer = customerRepository.save(customer);

        // DTO ile güvenli dönüş
        return new RegisterResponse(savedCustomer.getId());
    }

    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("invalid_credentials"));

        if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw new RuntimeException("invalid_credentials");
        }

        String token = jwtService.generateToken(customer.getId(), customer.getEmail());

        // Spec kurallarına uygun isimlendirme ve 1 saat (3600 sn) kuralı
        return new LoginResponse(token, 3600L);
    }
}