package com.aurora.auth_service.web;

import com.aurora.auth_service.service.AuthService;
import com.aurora.auth_service.web.dto.LoginRequest;
import com.aurora.auth_service.web.dto.RegisterRequest;
import com.aurora.auth_service.web.dto.LoginResponse;
import com.aurora.auth_service.web.dto.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        // ResponseEntity<Map> karmaşasından kurtulup direkt DTO dönüyoruz
        return authService.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // {"accessToken": "...", "expiresIn": 3600} formatında dönecek
        return authService.login(request);
    }
}